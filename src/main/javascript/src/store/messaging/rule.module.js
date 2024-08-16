import { messageRuleService } from "@/services";

const actions = {
  async get(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id, rule_id
    try {
      return await messageRuleService.get(...payload);
    } catch (e) {
      console.error("get catch", {e});
    }
  },
  async getForMessageContent(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id, rule_id
    try {
      return await messageRuleService.getForMessageContent(...payload);
    } catch (e) {
      console.error("getForMessageContent catch", {e});
    }
  },
  async create(_, payload) {
    // payload = experiment_id, condition_id, message_id, content_id
    try {
      const response = await messageRuleService.create(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, condition_id, message_id, content_id, rule_id, rule_dto
    try {
      return await messageRuleService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const rule = {
  namespaced: true,
  state: {},
  actions
}
