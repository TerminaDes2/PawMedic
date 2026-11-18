export function classifyFcmResponse(status, body) {
  if (status >= 200 && status < 300) return "sent";
  const details = Array.isArray(body?.error?.details) ? body.error.details : [];
  if (status === 404 && details.some((detail) => detail?.errorCode === "UNREGISTERED")) {
    return "invalid_token";
  }
  if (status === 401 || status === 403 || status === 404 || status === 429 || status >= 500) return "retry";
  return "permanent_failure";
}

export function notificationData(kind, id) {
  if (!["appointment_status", "appointment_reminder_24h", "appointment_reminder_1h"].includes(kind)) {
    throw new Error("Unsupported notification type");
  }
  if (typeof id !== "string" || !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) {
    throw new Error("Invalid notification identifier");
  }
  return { type: kind, delivery_id: id };
}
