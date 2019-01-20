<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form ref="alarm" :model="formItem" :rules="rules" :label-width="80">
                        <FormItem label="測站" prop="monitors">
                            <Select v-model="formItem.monitors" filterable multiple>
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
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset">取消</Button>
                        </FormItem>
                    </Form>                    
                </Card>
            </Col>
        </Row>
        <Row>
          <Col>
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
  name: "alarm",
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
      rules: {
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
      display: false,
      columns: [],
      rows: []
    };
  },
  computed: {
  },
  methods: {
    handleSubmit() {
      this.$refs.alarm.validate(valid => {
        if (valid) {
          this.query();
        }
      });
    },
    handleReset() {
      this.$refs.alarm.resetFields();
    },
    query() {
      this.display = true;
      this.formItem.start = this.formItem.dateRange[0].getTime();
      this.formItem.end = this.formItem.dateRange[1].getTime();
      axios
        .post("/QueryAlarm", this.formItem)
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
              date: new moment(row.date).format("lll")
            };
            for (let c = 0; c < row.cellData.length; c++) {
              let key = `col${c}`;
              rowData[key] = row.cellData[c].v;
              rowData.cellClassName = {
                name: row.cellData[c].cellClassName
              };
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
