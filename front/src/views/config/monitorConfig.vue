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
                        測站參數
                    </p>
                    <Row>
                        <Col>
                            <Card>
                                
                                    <can-edit-table 
                                        refs="monitorTab" 
                                        v-model="monitorList" 
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
import axios from "axios";

export default {
  name: "monitorConfig",
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
          title: "工業區",
          key: "indParkName"
        },
        {
          title: "測站",
          key: "dp_no"
        },
        {
          title: "緯度",
          key: "lat",
          editable: true
        },
        {
          title: "經度",
          key: "lng",
          editable: true
        },
        {
          title: "顯示在地圖上",
          key: "show",
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
      monitorList: []
    };
  },
  methods: {
    getConfig() {
      //Init monitorList
      axios
        .get("/Monitor")
        .then(resp => {
          this.monitorList.splice(0, this.monitorList.length);
          for (let monitor of resp.data) {
            if (monitor.show) monitor.show = "Y";
            else monitor.show = "N";

            this.monitorList.push(monitor);
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
      let id = this.monitorList[index]._id;
      this.$Message.success("删除了第" + (index + 1) + "行測項");
    },
    handleCellChange(val, index, key) {
      this.handleChange(val, index);
    },
    handleChange(val, index) {
      let id = this.monitorList[index]._id;
      let url = `/Monitor/${encodeURIComponent(id)}`;
      let arg = Object.assign({}, this.monitorList[index]);
      if (arg.lat) arg.lat = parseFloat(arg.lat);

      if (arg.lng) arg.lng = parseFloat(arg.lng);

      if (arg.show) {
        arg.show = arg.show === "Y";
      }

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
