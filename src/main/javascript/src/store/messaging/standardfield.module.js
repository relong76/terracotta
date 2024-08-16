import { messageStandardfieldService } from "@/services";

const actions = {
  async get(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id, standard_field_id
    try {
      return await messageStandardfieldService.get(...payload);
    } catch (e) {
      console.error("get catch", {e});
    }
  },
  async getForMessageContent(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id
    try {
      return await messageStandardfieldService.getForMessageContent(...payload);
    } catch (e) {
      console.error("getForMessageContent catch", {e});
    }
  },
  async create(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id
    try {
      const response = await messageStandardfieldService.create(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, condition_id, message_id, content_id, standard_field_id, custom_field_dto
    try {
      return await messageStandardfieldService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const standardfield = {
  namespaced: true,
  state: {},
  actions
}
