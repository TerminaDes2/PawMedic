import { PGlite } from "@electric-sql/pglite";
import { readFile } from "node:fs/promises";
import { strict as assert } from "node:assert";
import { test } from "node:test";

const user1 = "11111111-1111-4111-8111-111111111111";
const user2 = "22222222-2222-4222-8222-222222222222";
const pet = "33333333-3333-4333-8333-333333333333";
const appointment = "44444444-4444-4444-8444-444444444444";
const token1 = "test-device-token-111111111111111111";
const token2 = "test-device-token-222222222222222222";

test("notification drafts isolate users, schedule, cancel and retry deliveries", async () => {
  const db = new PGlite();
  try {
    await db.exec(`
      create role anon;
      create role authenticated;
      create role service_role;
      create schema auth;
      create table auth.users (id uuid primary key);
      create function auth.uid() returns uuid language sql stable as $$
        select nullif(current_setting('request.jwt.claim.sub', true), '')::uuid
      $$;
      create table public.pets (id uuid primary key, owner_id uuid references auth.users(id));
      create table public.appointments (
        id uuid primary key, pet_id uuid references public.pets(id),
        status text not null, starts_at timestamptz not null
      );
      insert into auth.users values ('${user1}'), ('${user2}');
      insert into public.pets values ('${pet}', '${user1}');
    `);
    for (const file of ["notification_devices.sql", "notification_outbox.sql", "notification_delivery.sql"]) {
      await db.exec(await readFile(new URL(`../drafts/${file}`, import.meta.url), "utf8"));
    }

    await db.exec(`select set_config('request.jwt.claim.sub', '${user1}', false);
      set role authenticated;
      select public.register_notification_device('${token1}', null);
      reset role;`);
    await assert.rejects(() => db.exec("set role authenticated; select * from public.notification_devices;"));
    await db.exec("reset role");
    await assert.rejects(() => db.exec("set role authenticated; select * from public.claim_appointment_notification_deliveries(100);"));
    await db.exec("reset role");
    await db.exec(`insert into public.appointments values
      ('${appointment}', '${pet}', 'REQUESTED', now() + interval '48 hours')`);
    assert.equal((await db.query("select count(*)::int as n from public.appointment_notification_outbox")).rows[0].n, 2);
    const offsets = await db.query(`select kind, extract(epoch from (appointment_start - due_at))::int as seconds
      from public.appointment_notification_outbox order by kind`);
    assert.deepEqual(offsets.rows, [
      { kind: "reminder_1h", seconds: 3600 },
      { kind: "reminder_24h", seconds: 86400 },
    ]);
    await db.exec(`update public.appointments set status = 'CONFIRMED' where id = '${appointment}'`);
    assert.equal((await db.query("select count(*)::int as n from public.appointment_notification_outbox")).rows[0].n, 3);
    await db.exec(`update public.appointment_notification_outbox set due_at = now() - interval '1 minute'
      where kind = 'reminder_24h'`);
    await db.exec("set role service_role; select public.prepare_appointment_notification_deliveries(100);");
    const claimed = await db.query("select * from public.claim_appointment_notification_deliveries(100)");
    assert.equal(claimed.rows.length, 2);
    assert.deepEqual(claimed.rows.map((row) => row.message_type).sort(), [
      "appointment_reminder_24h", "appointment_status",
    ]);
    for (const row of claimed.rows) {
      assert.equal(row.device_token, token1);
      await db.query("select public.finish_appointment_notification_delivery($1, $2, 'sent')", [row.delivery_id, row.lease_id]);
    }
    await db.exec("reset role");
    await db.exec(`update public.appointments set status = 'CANCELLED' where id = '${appointment}'`);
    assert.equal((await db.query("select count(*)::int as n from public.appointment_notification_outbox where kind = 'reminder_1h'")).rows[0].n, 0);
    await db.exec("set role service_role; select public.prepare_appointment_notification_deliveries(100)");
    const cancellation = (await db.query("select * from public.claim_appointment_notification_deliveries(100)")).rows[0];
    assert.equal(cancellation.message_type, "appointment_status");
    await db.query("select public.finish_appointment_notification_delivery($1, $2, 'sent')",
      [cancellation.delivery_id, "55555555-5555-4555-8555-555555555555"]);
    assert.equal((await db.query("select * from public.claim_appointment_notification_deliveries(100)")).rows.length, 0);
    await db.query("select public.finish_appointment_notification_delivery($1, $2, 'retry')",
      [cancellation.delivery_id, cancellation.lease_id]);
    assert.equal((await db.query("select * from public.claim_appointment_notification_deliveries(100)")).rows.length, 0);
    await db.exec("reset role");
    await db.query("update public.appointment_notification_deliveries set next_attempt_at = now() - interval '1 second' where id = $1",
      [cancellation.delivery_id]);
    await db.exec("set role service_role");
    const retry = (await db.query("select * from public.claim_appointment_notification_deliveries(100)")).rows[0];
    assert.notEqual(retry.lease_id, cancellation.lease_id);
    await db.query("select public.finish_appointment_notification_delivery($1, $2, 'sent')",
      [retry.delivery_id, retry.lease_id]);
    await db.exec("reset role");
    await db.exec(`select set_config('request.jwt.claim.sub', '${user1}', false);
      set role authenticated;
      select public.register_notification_device('${token2}', '${token1}');
      reset role;`);
    assert.equal((await db.query("select token from public.notification_devices")).rows[0].token, token2);
    await db.exec(`select set_config('request.jwt.claim.sub', '${user2}', false);
      set role authenticated;
      select public.unregister_notification_device('${token2}');
      reset role;`);
    assert.equal((await db.query("select count(*)::int as n from public.notification_devices")).rows[0].n, 1);
    await db.exec(`update public.appointments set status = 'CONFIRMED', starts_at = now() + interval '36 hours'
      where id = '${appointment}'`);
    await db.exec(`update public.appointments set starts_at = now() + interval '72 hours' where id = '${appointment}'`);
    const reminders = await db.query(`select item.kind, item.appointment_start = appointment.starts_at as current_time
      from public.appointment_notification_outbox item
      join public.appointments appointment on appointment.id = item.appointment_id
      where item.kind like 'reminder_%' and item.sent_at is null`);
    assert.equal(reminders.rows.length, 2);
    assert.ok(reminders.rows.every((row) => row.current_time));
    await db.exec(`update public.appointment_notification_outbox set due_at = now() - interval '1 minute'
      where kind = 'reminder_1h' and sent_at is null`);
    await db.exec("set role service_role; select public.prepare_appointment_notification_deliveries(100)");
    const later = await db.query("select * from public.claim_appointment_notification_deliveries(100)");
    assert.ok(later.rows.some((row) => row.message_type === "appointment_reminder_1h"));
  } finally {
    await db.close();
  }
});
