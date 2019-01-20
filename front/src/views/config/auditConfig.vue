<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col >
                <Card>
                    <Form ref="auditConfig" :model="formItem" :rules="rules" :label-width="80">
                        <FormItem label="測站" prop="monitor">
                            <Select v-model="formItem.monitor" filterable>
                                <Option v-for="item in monitorList" :value="item._id" :key="item._id">{{ item.dp_no }}</Option>
                            </Select>
                        </FormItem>
                         <FormItem>
                            <Button type="primary" @click="handleSubmit">儲存</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset('auditConfig')">取消</Button>
                        </FormItem>
                    </Form>                    
                </Card>
            </Col>
        </Row>
        <Row v-if="formItem.monitor">
          <Col>
            <Card >
              <Collapse v-model="configPage" accordion>
                <Panel name="1">
                  極大極小值
                  <div slot="content">
                    <min-max-rule2 :rule="activeConfig.minMaxRule"></min-max-rule2>
                  </div>                  
                </Panel>
                <Panel name="2">
                  合理性
                  <div slot="content">
                    說明:
                    由同一測站不同汙染物的從屬關係,進行合理性判斷.
                  </div>
                </Panel>
                <Panel name="3">
                  單調性
                  <div slot="content">
                    說明:
                    (測值-平均值)>?倍標準差
                  </div>
                </Panel>
                <Panel name="4">
                  突波高值
                  <div slot="content">
                    說明:
                    該測值超過前後平均的絕對值範圍
                  </div>
                </Panel>
                <Panel name="5">
                  持續性
                  <div slot="content">
                    說明:
                    連續多筆值相等, 視為連續性數值
                  </div>
                </Panel>
                <Panel name="6">
                  一致性
                  <div slot="content">
                    說明:
                    連續數小時之最高值與最低值之差小於系統設定值, 該測項小時值註記為異常
                  </div>
                </Panel>
                <Panel name="7">
                  小時值變換驗證
                  <div slot="content">
                    說明:
                    連續兩小時測值變化如超過系統設定之絕對值,該小時值註記為異常值
                  </div>
                </Panel>
                <Panel name="8">
                  三小時值變換驗證
                  <div slot="content">
                    說明:
                    連續三小時測值差距如超過系統設定之絕對值及比例, 該測項小時值註記為異常值
                  </div>
                </Panel>
                <Panel name="9">
                  四小時值變換驗證
                  <div slot="content">
                    說明:
                    連續四小時之平均值如大於系統設定值,該測項小時值註記為異常值
                  </div>
                </Panel>
                <Panel name="10">
                  分鐘值超過內控
                  <div slot="content">
                    說明:
                    分數值在一小時內超過內控值筆數
                  </div>
                </Panel>
                <Panel name="11">
                  分鐘值回傳超時
                  <div slot="content">
                    說明:
                    分數值在一小時內超過內控值筆數
                  </div>
                </Panel>
              </Collapse>
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
import minMaxRule2 from "./minMaxRule2.vue";

export default {
  name: "auditConfig",
  components: {
    minMaxRule2
  },
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
    //Get configs
    axios
      .get("/AuditConfig")
      .then(resp => {
        const ret = resp.data;
        this.auditConfigMap = Object.assign({}, this.auditConfigMap, ret);
      })
      .catch(err => {
        alert(err);
      });
    axios
      .get("/DefaultAuditConfig")
      .then(resp => {
        const ret = resp.data;
        this.activeConfig = Object.assign({}, this.activeConfig, ret);
      })
      .catch(err => {
        alert(err);
      });
  },
  watch: {
    "formItem.monitor": function(newValue, oldValue) {
    }
  },
  data() {
    return {
      monitorList: [],
      monitorTypeList: [],
      auditConfigMap: {},
      activeConfig: {},
      defaultAuditConfig: {},
      formItem: {
        monitor: undefined
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
        ]
      },
      configPage: "MinMax"
    };
  },
  computed: {},
  methods: {
    handleSubmit() {
      this.$refs.auditConfig.validate(valid => {
        if (valid) {
          this.query();
        }
      });
    },
    handleReset(name) {
      this.$refs[name].resetFields();
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
