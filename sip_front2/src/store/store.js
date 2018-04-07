import Vue from 'vue'
import Vuex from 'vuex'
import authenticated from './moudles/authenticated'

Vue.use(Vuex)
export const store = new Vuex.Store({
  state: {},
  getters: {},
  mutations: {},
  actions: {},
  modules: {
    authenticated
  }
})
