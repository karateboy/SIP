<style lang="less">
@import "../../styles/common.less";
@import "./components/table.less";
</style>

<template>
    <Card>
      <p slot="title">
      <Icon type="ios-keypad"></Icon>
        說明:汙染物濃度超過最大或最小值, 系統自動註記
      </p>
      <can-edit-table 
        refs="minMaxRuleTab" 
        v-model="ruleConfig" 
        @on-cell-change="handleCellChange" 
        @on-change="handleChange"
        @on-selection-change="OnSelectionChanged"
        :hover-show="true"  
        :editIncell="true" 
        :columns-list="columnsList"
      ></can-edit-table>
    </Card>                            
</template>

<script>
import canEditTable from "./components/canEditTable.vue";
import axios from "axios";

export default {
  name: "minMaxRule",
  components: {
    canEditTable
  },
  props: {
    rule: Object
  },
  data() {
    return {
      ruleEnabled: this.rule.enabled,
      ruleConfig: this.toRuleConfig(),
      columnsList: [
        {
          type: "selection",
          align: "center",
          width: 100
        },
        {
          title: "序號",
          type: "index",
          width: 80,
          align: "center"
        },
        {
          title: "測項",
          key: "mt"
        },
        {
          title: "最大值",
          key: "max",
          editable: true
        },
        {
          title: "最小值",
          key: "min",
          editable: true
        }
      ]
    };
  },
  computed: {},
  methods: {
    toRuleConfig() {
      let ruleConfig = [];
      for (let cfg of this.rule.monitorTypes) {
        let cfgObj = {
          _checked: cfg.enabled,
          mt: cfg.id,
          min: cfg.min,
          max: cfg.max
        };
        ruleConfig.push(cfgObj);
      }
      return ruleConfig;
    },
    handleCellChange(val, index, key) {
      //let id = this.monitorList[index]._id;
      //this.$Message.success(`修改${id}的${key}`);
      console.log(this.ruleConfig);
    },
    handleChange(val, index) {
      /*
      let id = this.monitorList[index]._id;
      let url = `/Monitor/${encodeURIComponent(id)}`;
      let arg = Object.assign({}, this.monitorList[index]);
      if (arg.lat) arg.lat = parseFloat(arg.lat);

      if (arg.lng) arg.lng = parseFloat(arg.lng);

      axios
        .post(url, arg)
        .then(resp => {
          const ret = resp.data;
          if (ret.ok) this.$Message.success(`修改${id}`);
        })
        .catch(err => {
          alert(err);
        });
        */
    },
    OnSelectionChanged(selection) {
      console.log(selection);
      return selection;
    },
    onSelectAll() {
      console.log("select all");
    }
  },
  created() {}
};
</script>
