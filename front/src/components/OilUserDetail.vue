<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-1 control-label">縣市:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.county"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">類型:</label>
                <div class="col-lg-4">
                <input type="radio" id="gasStation" value="gasStation" v-model="oilUser.useType">
                <label for="gasStation">加油站</label>

                <input type="radio" id="tank" value="tank" v-model="oilUser.useType">
                 <label for="tank">油槽</label>
                
                <input type="radio" id="boiler" value="boiler" v-model="oilUser.useType">
                <label for="boiler">鍋爐</label>
                
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">名稱:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.name"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">地址:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.addr"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">油槽數:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.tank"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">經度:</label>
                <div class="col-lg-2"><input type="number" class="form-control" v-model.number="oilUser.location[0]"></div>
                <label class="col-lg-1 control-label">緯度:</label>
                <div class="col-lg-2"><input type="number" class="form-control" v-model.number="oilUser.location[1]"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">總容積:</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="oilUser.quantity"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">品牌:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.brand"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">每月用量:</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="oilUser.usage"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">目前折讓(中油牌價為準):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="oilUser.discount"></div>
            </div>
            
            <div class="form-group">
                <label class="col-lg-1 control-label">簽約:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="oilUser.contracted">
                </div>
            </div>

            <div class="form-group">
                <label class="col-lg-1 control-label">業務:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.sales"></div>
            </div>

            <div class="form-group">
                <label class="col-lg-1 control-label">最後更新:</label>
                <div class="col-lg-4"><input type="date" class="form-control" v-model="lastUpdate">
                </div>
            </div>

            <div class="form-group">
                <label class="col-lg-1 control-label">備註:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="oilUser.remark"></div>
            </div>

            <div class="form-group">
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='save'>新增/更新</button>
                </div>
            </div>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
import axios from "axios";
import moment from "moment";

export default {
  props: {
    oilUser: {
      type: Object,
      required: true
    }
  },
  computed: {
    lastUpdate: {
      get() {
        if (this.oilUser.lastUpdate) {
          const date = new moment(this.oilUser.lastUpdate);
          return date.format("YYYY-MM-DD");
        } else return undefined;
      },
      set(v) {
        this.oilUser.lastUpdate = v;
      }
    }
  },
  methods: {
    vailidate() {
      if (!this.oilUser._id) {
        alert("_id不能是空的!");
        return false;
      }

      return true;
    },
    save() {
      if (!this.vailidate()) return;

      axios
        .put("/OilUser", this.oilUser)
        .then(resp => {
          let ret = resp.data;
          if (ret.Ok) {
            alert("成功!");
          }
        })
        .catch(err => alert(err));
    }
  },
  components: {}
};
</script>
