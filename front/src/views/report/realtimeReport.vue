<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
          <Col> 
            <realtime-map></realtime-map>
            <Card>
              <table class="table">
                <tbody>
                <tr>
	                <td class=" normal"><strong>標示說明:</strong></td>
	                <td class=" over_internal_std"><i class="fa fa-exclamation-triangle"></i>超過內控值</td>
	                <td class=" over_law_std"><i class="fa fa-exclamation-triangle"></i>超過法規值</td>
	                <td class=" normal calibration_status">校正</td>
	                <td class=" normal maintain_status">維修定保</td>
	                <td class=" normal abnormal_status">異常</td>

                	<td class=" normal auto_audit_status">自動檢核</td>
	                <td class=" normal manual_audit_status">人工註記</td>
                </tr>
                </tbody>
              </table>
            </Card>
            <Card>
              <Table height="600" :columns="columns" :data="rows"></Table>
            </Card>
          </Col>  
        </Row>
    </div>
</template>
<style scoped>
</style>
<script>
import Cookies from "js-cookie";
import axios from "axios";
import moment from "moment";
import realtimeMap from "../map/realtimeMap.vue";

export default {
  name: "realtimeReport",
  components: {
    realtimeMap
  },
  mounted() {
    this.query();
  },
  data() {
    return {
      monitorList: [],
      monitorTypeList: [],

      columns: [],
      rows: [],
      query_url: ""
    };
  },
  computed: {},
  methods: {
    query() {
      axios
        .get(`/RealtimeData`)
        .then(resp => {
          const ret = resp.data;
          this.columns.splice(0, this.columns.length);
          this.rows.splice(0, this.rows.length);
          for (let i = 0; i < ret.columnNames.length; i++) {
            let col = {
              title: ret.columnNames[i],
              minWidth: 110,
              key: `col${i}`,
              sortable: true
            };
            if (i === 0) col.fixed = "left";
            this.columns.push(col);
          }
          for (let row of ret.rows) {
            let rowData = {
              cellClassName: {}
            };
            for (let c = 0; c < row.cellData.length; c++) {
              let key = `col${c}`;
              rowData[key] = row.cellData[c].v;
              rowData.cellClassName[key] = row.cellData[c].cellClassName;
            }
            this.rows.push(rowData);
          }
        })
        .catch(err => {
          alert(err);
        });
    }
  }
};
</script>
