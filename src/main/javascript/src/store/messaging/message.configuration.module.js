import { messageConfigurationService } from "@/services";

const actions = {
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, message_id, configuration_id, configuration_dto
    try {
      const response = await messageConfigurationService.update(...payload);
      return response;
    } catch (e) {
      console.error("update catch", {e});
    }
  }
}

export const messageConfiguration = {
  namespaced: true,
  state: {},
  actions
}
