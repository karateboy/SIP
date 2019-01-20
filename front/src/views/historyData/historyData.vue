<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form :model="formItem" :label-width="80">
                        <FormItem label="測站">
                            <Select v-model="formItem.monitors" filterable multiple>
                                <Option v-for="item in monitorList" :value="item._id" :key="item._id">{{ item.dp_no }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="測項">
                            <Select v-model="formItem.monitorTypes" filterable multiple>
                                <Option v-for="item in monitorTypeList" :value="item._id" :key="item._id">{{ item.desp }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="資料區間">
                                <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm" 
                                    placeholder="選擇資料區間" style="width: 300px"
                                    v-model="formItem.dateRange"></DatePicker>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="query">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px">取消</Button>
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
  name: "historyData",
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
        dataType: "",
        monitors: [],
        monitorTypes: [],
        dateRange: "",
        start: undefined,
        end: undefined
      },
      display: false,
      columns: [],
      rows: []
    };
  },
  computed: {},
  methods: {
    query() {
      this.display = true;
      this.formItem.start = this.formItem.dateRange[0].getTime();
      this.formItem.end = this.formItem.dateRange[1].getTime();
      axios
        .post("/QueryRecord", this.formItem)
        .then(resp => {
          const ret = resp.data;
          this.columns.splice(0, this.columns.length);
          this.rows.splice(0, this.rows.length);
          this.columns.push({
            title: "日期",
            key: "date",
            sortable: true
          });
          for (let i = 0; i < ret.columnNames.length; i++) {
            let col = {
              title: ret.columnNames[i],
              key: `col${i}`,
              sortable: true
            };
            this.columns.push(col);
          }
          for (let row of ret.rows) {
            let rowData = {
              date: new moment(row.date).format("lll"),
              cellClassName: {}
            };
            for (let c = 0; c < row.cellData.length; c++) {
              let key = `col${c}`;
              rowData[key] = row.cellData[c].v;
              rowData.cellClassName[key] = row.cellData[c].cellClassName;
            }
            this.rows.push(rowData);
          }
          console.log(this.rows);
        })
        .catch(err => {
          alert(err);
        });
    }
  }
};
</script>
