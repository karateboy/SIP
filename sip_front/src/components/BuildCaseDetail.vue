<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-1 control-label">縣市:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.county"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">起造人案名:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.name"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">建築師:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.architect"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">地號:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.addr"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">送件日:</label>
                <div class="col-lg-4"><input type="date" class="form-control" v-model="myDate"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">經度:</label>
                <div class="col-lg-2"><input type="number" class="form-control" v-model.number="buildCase.location[0]"></div>
                <label class="col-lg-1 control-label">緯度:</label>
                <div class="col-lg-2"><input type="number" class="form-control" v-model.number="buildCase.location[1]"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">承造單位(含可能得標者):</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.builder"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">電話:</label>
                <div class="col-lg-4"><input type="tel" class="form-control" v-model="buildCase.phone"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">簽約:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="buildCase.contracted">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">最後拜訪日:</label>
                <div class="col-lg-4"><input type="date" class="form-control" v-model="lastVisit">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">業務:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="buildCase.sales"></div>
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
    buildCase: {
      type: Object,
      required: true
    }
  },
  computed: {
    myDate: {
      get() {
        const date = new moment(this.buildCase.date);
        return date.format("YYYY-MM-DD");
      },
      set(v) {
        this.buildCase.date = v;
      }
    },
    lastVisit: {
      get() {
        if (this.buildCase.lastVisit) {
          const date = new moment(this.buildCase.lastVisit);
          return date.format("YYYY-MM-DD");
        } else return undefined;
      },
      set(v) {
        this.buildCase.lastVisit = v;
      }
    }
  },
  methods: {
    vailidate() {
        if(!this.buildCase.addr){
            alert("地號不能是空的!")
            return false
        }

        if(!this.buildCase.name){
            alert("起造人案名不能是空的!")
            return false
        }

        if(!this.buildCase.date){
            alert("送件日不能是空的!")
            return false
        }

        if(!this.buildCase.architect){
            alert("建築師不能是空的!")
            return false
        }

        return true
    },
    save() {
      if(!this.vailidate())
          return 

      if (this.buildCase._id != this.buildCase.addr) {
        this.buildCase._id == this.buildCase.addr;
      }

      axios
        .put("/BuildCase", this.buildCase)
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
