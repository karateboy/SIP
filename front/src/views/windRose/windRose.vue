<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form ref="windRose" :model="formItem" :rules="rules" :label-width="80">
                        <FormItem label="測站" prop="monitors">
                            <Select v-model="formItem.monitors" filterable>
                                <Option v-for="item in monitorList" :value="item._id" :key="item._id">{{ item.dp_no }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="測項" prop="monitorTypes">
                            <Select v-model="formItem.monitorTypes" filterable>
                                <Option v-for="item in monitorTypeList" :value="item._id" :key="item._id">{{ item.desp }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="方位" prop="nWay">
                            <Select v-model="formItem.nWay" filterable>
                                <Option v-for="nWay in nWayList" :value="nWay" :key="nWay">{{ nWay }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem label="資料區間" prop="dateRange">
                                <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm" 
                                    placeholder="選擇資料區間" style="width: 300px"
                                    v-model="formItem.dateRange"></DatePicker>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="handleSubmit">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset('windRose')">取消</Button>
                            <Button type="ghost" style="margin-left: 8px" icon="document" :disabled="!downloadable" @click="downloadExcel">下載Excel</Button>
                        </FormItem>
                    </Form>                    
                </Card>
            </Col>
        </Row>
        <Row>
            <Card v-if="display">
              <div id="reportDiv"></div>
            </Card>
        </Row>
    </div>
</template>
<style scoped>
</style>
<script>
import highcharts from "highcharts";
import highchart_more from "highcharts/highcharts-more";
import Cookies from "js-cookie";
import axios from "axios";
import moment from "moment";
import baseUrl from "../../baseUrl";
export default {
  name: "windRose",

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
        monitors: undefined,
        monitorTypes: undefined,
        dateRange: [],
        nWay: 8,
        start: undefined,
        end: undefined
      },
      rules: {
        monitors: [
          {
            required: true,
            type: "string",
            message: "測站不能是空的",
            trigger: "change"
          }
        ],
        monitorTypes: [
          {
            required: true,
            type: "string",
            min: 1,
            message: "測項不能是空的",
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
      nWayList: [8, 16, 32],
      display: false,
      query_url: ""
    };
  },
  computed: {
    downloadable() {
      return this.query_url.length != 0;
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.windRose.validate(valid => {
        console.log(valid);
        if (valid) {
          this.query();
        }
      });
    },
    handleReset(name) {
      this.$refs[name].resetFields();
    },
    downloadExcel() {
      let url = baseUrl() + `/Excel/WindRose/${this.query_url}`;
      window.open(url);
    },
    query() {
      this.display = true;
      let monitors = encodeURIComponent(this.formItem.monitors);
      let monitorTypes = encodeURIComponent(this.formItem.monitorTypes);
      let start = this.formItem.dateRange[0].getTime();
      let end = this.formItem.dateRange[1].getTime();
      this.query_url = `${monitors}/${monitorTypes}/${
        this.formItem.nWay
      }/${start}/${end}`;

      axios
        .get("/JSON/WindRose/" + this.query_url)
        .then(resp => {
          if (resp.status != 200) {
            this.$Message.error("沒有資料!");
            return;
          }

          let result = resp.data;

          result.pane = {
            size: "90%"
          };
          result.legend = {
            align: "right",
            verticalAlign: "top",
            y: 100,
            layout: "vertical"
          };
          result.yAxis = {
            min: 0,
            endOnTick: false,
            showLastLabel: true,
            title: {
              text: "頻率 (%)"
            },
            labels: {
              formatter: function() {
                return this.value + "%";
              }
            },
            reversedStacks: false
          };

          result.tooltip = {
            valueDecimals: 2,
            valueSuffix: "%"
          };

          result.plotOptions = {
            series: {
              stacking: "normal",
              shadow: false,
              groupPadding: 0,
              pointPlacement: "on"
            }
          };
          result.credits = {
            enabled: false,
            href: "http://www.wecc.com.tw/"
          };
          result.title.x = -70;
          highchart_more(highcharts);
          let myChart = highcharts.chart("reportDiv", result);
        })
        .catch(err => {
          this.$Message.error("沒有資料!");
        });
    }
  }
};
</script>
