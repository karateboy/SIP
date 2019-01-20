<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form ref="monitorReport" :model="formItem" :rules="rules" :label-width="80">
                        <FormItem label="測站" prop="monitor">
                            <Select v-model="formItem.monitor" filterable>
                                <Option v-for="item in monitorList" :value="item._id" :key="item._id">{{ item.dp_no }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="報表種類" prop="reportType">
                            <Select v-model="formItem.reportType" filterable>
                                <Option v-for="rt in reportTypes" :value="rt.id" :key="rt.id">{{ rt.desp }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="查詢日期" prop="date">
                                <DatePicker :type="datePickerType"
                                    placeholder="選擇查詢日期" style="width: 300px"
                                    v-model="formItem.date"></DatePicker>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="handleSubmit">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset">取消</Button>
                            <Button type="ghost" style="margin-left: 8px" icon="document" :disabled="!downloadable" @click="downloadExcel">下載Excel</Button>
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
            <Card v-if="display">
              <Table height="500" :columns="columns" :data="rows"></Table>
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
import baseUrl from "../../baseUrl";

export default {
  name: "monitorReport",
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
  },
  data() {
    return {
      monitorList: [],
      formItem: {
        monitor: undefined,
        reportType: undefined,
        date: undefined,
        start: undefined
      },
      rules: {
        monitor: [
          {
            required: true,
            type: "string",
            message: "請選擇測站",
            trigger: "change"
          }
        ],
        reportType: [
          { required: true, message: "請選擇報表種類", trigger: "change" }
        ],
        date: [
          {
            required: true,
            type: "date",
            message: "請選擇報表日期",
            trigger: "change"
          }
        ]
      },
      reportTypes: [
        {
          id: "daily",
          desp: "日報"
        },
        {
          id: "monthly",
          desp: "月報"
        }
      ],
      display: false,
      columns: [],
      rows: [],
      query_url: ""
    };
  },
  computed: {
    datePickerType() {
      if (this.formItem.reportType === "monthly") return "month";
      else if (this.formItem.reportType === "yearly") return "year";
      else return "date";
    },
    downloadable() {
      return this.query_url.length != 0;
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.monitorReport.validate(valid => {
        if (valid) {
          this.query();
        }
      });
    },
    handleReset() {
      this.$refs.monitorReport.resetFields();
    },
    downloadExcel() {
      let url = baseUrl() + `/Excel/monitorReport/${this.query_url}`;
      window.open(url);
    },
    query() {
      this.display = true;
      let monitor = encodeURIComponent(this.formItem.monitor);
      let start = this.formItem.date.getTime();

      this.query_url = `${monitor}/${this.formItem.reportType}/${start}`;
      axios
        .get(`/JSON/monitorReport/${this.query_url}`)
        .then(resp => {
          const ret = resp.data;
          this.columns.splice(0, this.columns.length);
          this.rows.splice(0, this.rows.length);
          for (let i = 0; i < ret.columnNames.length; i++) {
            let col = {
              title: ret.columnNames[i],
              minWidth: 100,
              key: `col${i}`
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
