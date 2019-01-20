<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col >
                <Card>
                    <Form ref="auditReport" :model="formItem" :rules="rules" :label-width="80">
                        <FormItem label="測站" prop="monitor">
                            <Select v-model="formItem.monitor" filterable>
                                <Option v-for="item in monitorList" :value="item._id" :key="item._id">{{ item.dp_no }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="測項" prop="monitorTypes">
                            <Select v-model="formItem.monitorTypes" filterable multiple>
                                <Option v-for="item in monitorTypeList" :value="item._id" :key="item._id">{{ item.desp }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="資料區間" prop="dateRange">
                                <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm" 
                                    placeholder="選擇資料區間" style="width: 300px"
                                    v-model="formItem.dateRange"></DatePicker>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="handleSubmit">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset('auditReport')">取消</Button>
                            <Button type="ghost" style="margin-left: 8px" icon="document" :disabled="true" @click="downloadExcel">下載Excel</Button>
                        </FormItem>
                    </Form>                    
                </Card>
            </Col>
        </Row>
        <Row>
          <Col>
            <Card>
              <table class="table">
                <tbody>
                <tr>
	                <td class="col-lg-1 normal text-center"><strong>標示說明:</strong></td>
	                <td class="col-lg-1 over_internal_std text-center"><i class="fa fa-exclamation-triangle"></i>超過內控值</td>
	                <td class="col-lg-1 over_law_std text-center"><i class="fa fa-exclamation-triangle"></i>超過法規值</td>
	                <td class="col-lg-1 normal calibration_status text-center">校正</td>
	                <td class="col-lg-1 normal maintain_status text-center">維修定保</td>
	                <td class="col-lg-1 normal abnormal_status text-center">異常</td>

                	<td class="col-lg-1 normal auto_audit_status text-center">自動檢核</td>
	                <td class="col-lg-1 normal manual_audit_status text-center">人工註記</td>
                </tr>
                </tbody>
              </table>
            </Card>
            <Card v-if="display">
              <Table :columns="columns" :data="rows"></Table>
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
export default {
  name: "auditReport",
  mounted() {
    //Init monitorList
    axios
      .get("/Monitor")
      .then(resp => {
        this.monitorList.splice(0, this.monitorList.length);
        for (let monitor of resp.data) {
          this.monitorList.push(monitor);
        }
      })
      .catch(err => {
        alert(err);
      });
    //Init monitorTypeList
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
  data() {
    return {
      monitorList: [],
      monitorTypeList: [],
      formItem: {
        monitor: undefined,
        monitorTypes: [],
        dateRange: undefined
      },
      display: false,
      rules: {
        monitor: [
          {
            required: true,
            type: "string",
            message: "請選擇測站",
            trigger: "change"
          }
        ],
        monitorTypes: [
          {
            required: true,
            type: "array",
            min: 1,
            message: "至少選擇一個測項",
            trigger: "change"
          }
        ],
        dateRange: [
          {
            required: true,
            type: "array",
            min: 1,
            message: "請選擇資料範圍",
            trigger: "change"
          }
        ]
      },

      columns: [],
      rows: []
    };
  },
  computed: {},
  methods: {
    handleSubmit() {
      this.$refs.auditReport.validate(valid => {
        if (valid) {
          this.query();
        }
      });
    },
    handleReset(name) {
      this.$refs[name].resetFields();
    },
    downloadExcel() {
      let url = baseUrl() + `/Excel/AuditReport/${this.query_url}`;
      window.open(url);
    },
    query() {
      this.display = true;
      let monitor = encodeURIComponent(this.formItem.monitor);
      let monitorTypes = encodeURIComponent(
        this.formItem.monitorTypes.join(":")
      );
      let start = this.formItem.dateRange[0].getTime();
      let end = this.formItem.dateRange[1].getTime();
      this.query_url = `${monitors}/${monitorTypes}/${start}/${end}`;

      axios
        .get(`/JSON/AuditReport/`)
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
