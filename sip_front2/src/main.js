// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App'
import router from './router'
import axios from 'axios'
import baseUrl from './baseUrl'
import moment from 'moment'
import {
  store
} from './store/store'
import * as VueGoogleMaps from 'vue2-google-maps'
import locale from 'element-ui/lib/locale/lang/zh-tw'

Vue.config.productionTip = false

// Setup axios config
axios.defaults.baseURL = baseUrl()
axios.defaults.withCredentials = true

// Setup moment
moment.locale('zh_tw')

Vue.use(VueGoogleMaps, {
  load: {
    key: 'AIzaSyAi9hG7X74_CL-3i_6utBMNKzrRKOqwo98',
    libraries: 'places' // This is required if you use the Autocomplete plugin
    // OR: libraries: 'places,drawing'
    // OR: libraries: 'places,drawing,visualization'
    // (as you require)
  }
})
Vue.use(ElementUI, {
  locale
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store,
  router,
  components: {
    App
  },
  template: '<App/>'
})
