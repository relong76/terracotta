var status = {
  attachment: {
    created: "CREATED",
    deleted: "DELETED",
    error: "ERROR",
    uploaded: "UPLOADED"
  },
  message: {
    copied: "COPIED",
    created: "CREATED",
    edited: "EDITED",
    ready: "READY",
    canceled: "CANCELED",
    deleted: "DELETED",
    error: "ERROR",
    processing: "PROCESSING",
    queued: "QUEUED",
    sent: "SENT"
  }
};

export var attachment = status.attachment;
export var message = status.message;

export var editableMessageStatuses = [
  message.copied,
  message.created,
  message.edited,
  message.ready
];
