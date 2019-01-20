import Vue from 'vue';
import iView from 'iview';
import {
    router
} from './router/index';
import {
    appRouter
} from './router/router';
import store from './store';
import App from './app.vue';
import '@/locale';
import 'iview/dist/styles/iview.css';
import locale from 'iview/dist/locale/zh-TW';
import VueI18n from 'vue-i18n';
import util from './libs/util';
import axios from 'axios';
import baseUrl from './baseUrl';
import moment from 'moment';
import Highcharts from 'highcharts';
import * as VueGoogleMaps from 'vue2-google-maps';

// Setup axios config
axios.defaults.baseURL = baseUrl();
axios.defaults.withCredentials = true;
// Setup moment
moment.locale('zh_tw');
Vue.use(VueI18n);
Vue.use(iView, {
    locale
});

Highcharts.setOptions({
    global: {
        useUTC: false
    },
    lang: {
        contextButtonTitle: '圖表功能表',
        downloadJPEG: '下載JPEG',
        downloadPDF: '下載PDF',
        downloadPNG: '下載PNG',
        downloadSVG: '下載SVG',
        drillUpText: '回到{series.name}.',
        noData: '無資料',
        months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        printChart: '列印圖表',
        resetZoom: '重設放大區間',
        resetZoomTitle: '回到原圖大小',
        shortMonths: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        weekdays: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    }
});

Vue.use(VueGoogleMaps, {
    load: {
        key: 'AIzaSyAF2H8azbXiecvCre_b1S8UGyb24aqjqj0',
        libraries: 'places' // This is required if you use the Autocomplete plugin
        // OR: libraries: 'places,drawing'
        // OR: libraries: 'places,drawing,visualization'
        // (as you require)
    }
});

new Vue({
    el: '#app',
    router: router,
    store: store,
    render: h => h(App),
    data: {
        currentPageName: ''
    },
    mounted() {
        this.currentPageName = this.$route.name;
        // 显示打开的页面的列表
        this.$store.commit('setOpenedList');
        this.$store.commit('initCachepage');
        // 权限菜单过滤相关
        this.$store.commit('updateMenulist');
        // iview-admin检查更新
        util.checkUpdate(this);
    },
    created() {
        let tagsList = [];
        appRouter.map((item) => {
            if (item.children.length <= 1) {
                tagsList.push(item.children[0]);
            } else {
                tagsList.push(...item.children);
            }
        });
        this.$store.commit('setTagsList', tagsList);
    }
});