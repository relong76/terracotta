import { messageGroupService } from "@/services";

const state = {
  groupMessages: [],
  groupMessagesExpanded: []
}
const actions = {
  async get(_, payload) {
    // payload = experiment_id, exposure_id, group_id
    try {
      return await messageGroupService.get(...payload);
    } catch (e) {
      console.error("get catch", {e});
    }
  },
  async getAll(_, payload) {
    // payload = experiment_id, exposure_id
    try {
      return await messageGroupService.getAll(...payload);
    } catch (e) {
      console.error("getAll catch", {e});
    }
  },
  async create(_, payload) {
    // payload = experiment_id, exposure_id, single
    try {
      const response = await messageGroupService.create(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("create catch", {e});
    }
  },
  async update(_, payload) {
    // payload: experiment_id, exposure_id, group_id, group_dto
    try {
      return await messageGroupService.update(...payload);
    } catch (e) {
      console.error("update catch", {e});
    }
  },
  async send(_, payload) {
    // payload = experiment_id, exposure_id, group_id
    try {
      const response = await messageGroupService.send(...payload);

      if (response?.id) {
        return response;
      }
    } catch (e) {
      console.error("send catch", {e});
    }
  },
  async deleteGroup(_, payload) {
    // payload: experiment_id, exposure_id, group_id
    try {
      return await messageGroupService.deleteGroup(...payload);
    } catch (e) {
      console.error("delete catch", {e});
    }
  },
  async move(_, payload) {
    // payload: experiment_id, exposure_id, group_id, group_dto
    try {
      const response = await messageGroupService.move(...payload);
      if (response?.id) {
        return {
          status: 201,
          data: response
        }
      }
    } catch (error) {
      console.error("move catch", {error});
    }
  },
  async duplicate(_, payload) {
    // payload: experiment_id, exposure_id, group_id
    try {
      const response = await messageGroupService.duplicate(...payload);
      if (response?.id) {
        return {
          status: 201,
          data: response
        }
      }
    } catch (error) {
      console.error("duplicate catch", {error});
    }
  },
  async reset({commit}) {
    commit("setGroupMessages", []);
    commit("setGroupMessagesExpanded", []);
  },
}
const mutations = {
  setGroupMessages(state, groupMessages) {
    state.groupMessages = groupMessages
  },
  setGroupMessagesExpanded(state, groupMessagesExpanded) {
    state.groupMessagesExpanded = groupMessagesExpanded
  }
}
const getters = {
  groupMessages: (state) => {
    return state.groupMessages;
  },
  groupMessagesExpanded: (state) => {
    return state.groupMessagesExpanded;
  }
}
export const group = {
  namespaced: true,
  state,
  actions,
  mutations,
  getters
}
