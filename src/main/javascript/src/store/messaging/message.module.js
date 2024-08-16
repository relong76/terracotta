import { messageService } from "@/services";

const actions = {
  async get(_, payload) {
    // payload = experiment_id, exposure_id, group_id, message_id
    try {
      return await messageService.get(...payload);
    } catch (e) {
      console.error("get catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, message_id, message_dto
    try {
      return await messageService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const message = {
  namespaced: true,
  state: {},
  actions
}
