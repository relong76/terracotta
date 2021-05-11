import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'

import { account } from './account.module'
import { alert } from './alert.module'

Vue.use(Vuex)

const store = new Vuex.Store({
    plugins: [createPersistedState({
        key: 'terracotta'
    })],
    modules: {
        account,
        alert
    }
})

export default store