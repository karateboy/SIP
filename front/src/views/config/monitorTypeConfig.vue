<style lang="less">
@import "../../styles/common.less";
@import "./components/table.less";
</style>

<template>
    <div>
        <Row class="margin-top-10">
            <Col span="24">
                <Card>
                    <p slot="title">
                        <Icon type="ios-keypad"></Icon>
                        測項參數
                    </p>
                    <Row>
                        <Col>
                            <Card>
                                
                                    <can-edit-table 
                                        refs="table4" 
                                        v-model="monitorTypeList" 
                                        @on-cell-change="handleCellChange" 
                                        @on-change="handleChange"
                                        :hover-show="true"  
                                        :editIncell="true" 
                                        :columns-list="columnsList"
                                    ></can-edit-table>
                            </Card>                            
                        </Col>
                    </Row>
                </Card>
            </Col>
        </Row>
    </div>
</template>

<script>
import canEditTable from "./components/canEditTable.vue";
import tableData from "./components/table_data.js";
import axios from "axios";

export default {
  name: "monitorTypeConfig",
  components: {
    canEditTable
  },
  data() {
    return {
      columnsList: [
        {
          title: "序號",
          type: "index",
          width: 80,
          align: "center"
        },
        {
          title: "名稱",
          key: "desp",
          editable: true
        },
        {
          title: "單位",
          key: "unit",
          editable: true
        },
        {
          title: "警報值",
          key: "std_internal",
          editable: true
        },
        {
          title: "超高警報值",
          key: "std_law",
          editable: true
        },
        {
          title: "小數點位數",
          key: "prec",
          editable: true
        },
        {
          title: "標準1",
          key: "level1",
          editable: true
        },
        {
          title: "標準2",
          key: "level2",
          editable: true
        },
        {
          title: "標準3",
          key: "level3",
          editable: true
        },
        {
          title: "標準4",
          key: "level4",
          editable: true
        },
        {
          title: "操作",
          align: "center",
          width: 200,
          key: "handle",
          handle: ["edit"]
        }
      ],
      monitorTypeList: []
    };
  },
  methods: {
    getConfig() {
      axios
        .get("/MonitorType")
        .then(resp => {
          this.monitorTypeList.splice(0, this.monitorTypeList.length);
          for (let mt of resp.data) {
            this.monitorTypeList.push(mt);
          }
        })
        .catch(err => {
          alert(err);
        });
    },
    handleNetConnect(state) {
      this.breakConnect = state;
    },
    handleLowSpeed(state) {
      this.lowNetSpeed = state;
    },
    getCurrentData() {
      this.showCurrentTableData = true;
    },
    handleDel(val, index) {
      let id = this.monitorTypeList[index]._id;
      this.$Message.success("删除了第" + (index + 1) + "行測項");
    },
    handleCellChange(val, index, key) {
      return this.handleChange(val, index);
    },
    handleChange(val, index) {
      let id = this.monitorTypeList[index]._id;
      let url = `/MonitorType/${encodeURIComponent(id)}`;
      let arg = Object.assign({}, this.monitorTypeList[index]);
      arg.level1 = parseFloat(arg.level1);
      arg.level2 = parseFloat(arg.level2);
      arg.level3 = parseFloat(arg.level3);
      arg.level4 = parseFloat(arg.level4);
      arg.std_internal = parseFloat(arg.std_internal);
      arg.std_law = parseFloat(arg.std_law);

      axios
        .post(url, arg)
        .then(resp => {
          const ret = resp.data;
          if (ret.ok) this.$Message.success(`修改${id}`);
        })
        .catch(err => {
          alert(err);
        });
    }
  },
  created() {
    this.getConfig();
  }
};
</script>
