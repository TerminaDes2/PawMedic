import { strict as assert } from "node:assert";
import { test } from "node:test";
import { classifyFcmResponse, notificationData } from "../functions/_shared/notification-fcm.mjs";

test("FCM results distinguish invalid tokens from retryable failures", () => {
  assert.equal(classifyFcmResponse(200, {}), "sent");
  assert.equal(classifyFcmResponse(404, { error: { details: [{ errorCode: "UNREGISTERED" }] } }), "invalid_token");
  for (const status of [401, 403, 404, 429, 500, 503]) {
    assert.equal(classifyFcmResponse(status, {}), "retry");
  }
});

test("notification payload contains no appointment or user information", () => {
  const id = "b2640df6-9a42-4bb0-8d9c-9c038f6bd704";
  assert.deepEqual(notificationData("appointment_reminder_24h", id), {
    type: "appointment_reminder_24h",
    delivery_id: id,
  });
  assert.throws(() => notificationData("unknown", id));
  assert.throws(() => notificationData("appointment_status", "not-an-id"));
});
