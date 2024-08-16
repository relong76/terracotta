import { messageService } from "@/services";

const actions = {
  async get(_, payload) {
    // payload = experiment_id, condition_id, message_id
    try {
      return await messageService.get(...payload);
    } catch (e) {
      console.error("get catch", {e});
    }
  },
  async getAll(_, payload) {
    // payload = experiment_id, condition_id
    try {
      return await messageService.getAll(payload);
    } catch (e) {
      console.error("getAll catch", {e});
    }
  },
  async create(_, payload) {
    // payload = experiment_id, condition_id
    try {
      const response = await messageService.create(payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, condition_id, message_id, message_dto
    try {
      return await messageService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  },
  async copy(_, payload) {
    // payload: experiment_id, condition_id, message_id, from_message_dto
    try {
      return await messageService.copy(...payload);
    } catch (e) {
      console.error("copy catch", {e});
    }
  }
}

export const message = {
  namespaced: true,
  state: {},
  actions
}
