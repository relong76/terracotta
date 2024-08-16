import { messageContentService } from "@/services";

const actions = {
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, message_id, content_id, content_dto
    try {
      const response =  await messageContentService.update(...payload);
      return response;
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const content = {
  namespaced: true,
  state: {},
  actions
}
