<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-1 control-label">名稱:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="名稱" v-model="queryParam.name"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">縣市:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="新北市" v-model="queryParam.county"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">地址:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="地址" v-model="queryParam.addr"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">類型:</label>
                <div class="col-lg-4">
                <input type="checkbox" id="gasStation" value="gasStation" v-model="queryParam.useType">
                <label for="gasStation">加油站</label>

                <input type="checkbox" id="tank" value="tank" v-model="queryParam.useType">
                 <label for="tank">油槽</label>
                
                <input type="checkbox" id="boiler" value="boiler" v-model="queryParam.useType">
                <label for="boiler">鍋爐</label>
                
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">是否簽約:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="queryParam.contracted">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">業務:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="業務" v-model="queryParam.sales"></div>
            </div>

            <div class="form-group">
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='query'>查詢</button>
                </div>
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='queryMap'>顯示地圖</button>
                </div>
                <div v-if="user.groupId == 'Admin'" class="col-lg-1 col-lg-offset-1">
                    <button class="btn btn-info" @click='exportExcel'>下載Excel</button>
                </div>
            </div>
        </div>
        <div v-if='display'>
            <oil-user-list url="/QueryOilUser" :param="queryParam"></oil-user-list>
        </div>
        <div v-if='showMap'>
            <oil-user-map url="/QueryOilUser" :param="queryParam"></oil-user-map>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
import axios from "axios";
import moment from "moment";
import { mapGetters } from "vuex";
import baseUrl from "../baseUrl";

import OilUserList from "./OilUserList.vue";
import OilUserMap from "./OilUserMap.vue";

export default {
  data() {
    return {
      display: false,
      showMap: false,
      queryParam: {
        name: undefined,
        county: undefined,
        addr: undefined,
        useType: [],
        contracted: undefined,
        sales: undefined
      }
    };
  },
  computed: {
    ...mapGetters(["user"])
  },
  methods: {
    query() {
      if (!this.display) this.display = true;

      this.queryParam = Object.assign({}, this.queryParam);
    },
    queryMap() {
      if (!this.showMap) this.showMap = true;

      this.queryParam = Object.assign({}, this.queryParam);
    },
    exportExcel() {
      let json = JSON.stringify(this.queryParam);
      let segment = encodeURIComponent(json);
      let url = baseUrl() + "/OilUser/" + segment;
      window.open(url);
    }
  },
  components: {
    OilUserList,
    OilUserMap
  }
};
</script>
