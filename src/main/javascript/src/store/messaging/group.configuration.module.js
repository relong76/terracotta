import { groupConfigurationService } from "@/services";

const actions = {
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, configuration_id, group_configuration_dto
    try {
      const response = await groupConfigurationService.update(...payload);
      return response;
    } catch (e) {
      console.error("update catch", {e});
    }
  },
  async updateAll(_, payload) {
    // payload: experiment_id, exposure_id, group_configuration_dtos
    try {
      const response = await groupConfigurationService.updateAll(...payload);
      return response;
    } catch (e) {
      console.error("updateAll catch", {e});
    }
  }
}

export const groupConfiguration = {
  namespaced: true,
  state: {},
  actions
}
