<template>
    <div>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-sm-2 control-label">起造人名稱:</label>
                <div class="col-sm-4">
                    <input type="text" placeholder="起造人名稱" class="form-control"
                           required v-model="builder._id">
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">地址:</label>
                <div class="col-sm-4">
                    <input type="text" placeholder="地址" class="form-control"
                           required v-model="builder.addr">
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">聯絡人:</label>
                <div class="col-sm-4">
                    <input type="text" placeholder="地址" class="form-control"
                           required v-model="builder.contact">
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">電話:</label>
                <div class="col-sm-4">
                    <input type="text" placeholder="電話" class="form-control"
                           required v-model="builder.phone">
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-2">
                    <button class="btn btn-primary" @click.prevent="submit" :disabled="!builder.phone">更新</button>
                    <button class="btn btn-primary" @click.prevent="giveUp">找不到電話</button>
                </div>
            </div>
        </div>
    </div>
</template>
<style>
body {
  background-color: #ff0000;
}
</style>
<script>
import axios from "axios";
import { mapActions } from "vuex";

export default {
  data() {
    return {
      builder: {}
    };
  },
  computed: {},
  mounted(){
      this.checkOut()
  },
  methods: {
    submit() {
      this.builder.state = 1;
      this.updateBuilder();
    },
    giveUp() {
      this.builder.state = 2;
      this.updateBuilder();
    },
    checkOut() {
      axios
        .get("/CheckOutBuilder")
        .then(resp => {            
          const ret = resp.data;
          const status = resp.status
          if(status == 200){
            this.builder = JSON.parse(JSON.stringify(ret));
            console.log(this.builder)
          }            
          else{
            alert("已無起造人待更新, 請稍後再試")
          }
        })
        .catch(err => alert("err"));
    },
    updateBuilder() {
      console.log(this.builder);

      axios
        .post("/Builder", this.builder)
        .then(resp => {
          const ret = resp.data;
          if (ret.ok) {
            alert("成功");
            this.checkOut();
          } else alert("失敗:" + ret.msg);
        })
        .catch(err => {
          alert(err);
        });
    }
  },
  components: {}
};
</script>
