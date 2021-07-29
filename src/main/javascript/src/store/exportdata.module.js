import {exportdataService} from '@/services'

const state = {
  file: null,
}

const actions = {
  fetchExportData: ({commit}, experimentId) => {
    console.log('Here in Module', experimentId)
    return exportdataService.getZip(experimentId)
      .then(response => {
          console.log('Response is: ', response)
        if (response.status===200) {
          commit('setExportData', response.data)
        }
      })
      .catch(response => console.log('fetchExportData | catch', {response}))
  }
}

const mutations = {
  setExportData(state, data) {
    state.file = data
  },
}

const getters = {
  exportData(state) {
    return state.file
  },
}

export const exportdata = {
  namespaced: true,
  state,
  actions,
  mutations,
  getters
}