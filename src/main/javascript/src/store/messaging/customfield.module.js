import { messageCustomfieldService } from "@/services";

const actions = {
  async create(_, payload) {
    // payload = experiment_id, exposure_id, group_id, message_id, content_id
    try {
      const response = await messageCustomfieldService.create(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, message_id, content_id, custom_field_id, custom_field_dto
    try {
      return await messageCustomfieldService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const customfield = {
  namespaced: true,
  state: {},
  actions
}
