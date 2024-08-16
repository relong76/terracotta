import { messageContentAttachmentService } from "@/services";

const actions = {
  async create(_, payload) {
    // payload = experiment_id, exposure_id, group_id, message_id, content_id, file
    try {
      const response = await messageContentAttachmentService.create(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async remove(_, payload) {
    // payload: experiment_id, exposure_id, group_id, message_id, content_id, file_id
    try {
      const response = await messageContentAttachmentService.remove(...payload);
      return response;
    } catch (e) {
      console.error("remove catch", {e});
    }
  }
}

export const attachment = {
  namespaced: true,
  state: {},
  actions
}
