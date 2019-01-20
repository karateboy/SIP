<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form ref="realtimeTrend" :model="formItem" :rules="rules" :label-width="80">
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
                        <FormItem label="單位" prop="reportUnit">
                            <Select v-model="formItem.reportUnit" filterable>
                                <Option v-for="unit in reportUnit" :value="unit.id" :key="unit.id">{{ unit.desc }}</Option>
                            </Select>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="handleSubmit">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset('realtimeTrend')">取消</Button>
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
import Cookies from "js-cookie";
import axios from "axios";
import moment from "moment";
import baseUrl from "../../baseUrl";
export default {
  name: "realtimeTrend",

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
      reportUnit: [
        {
          id: "Min",
          desc: "分"
        },
        {
          id: "Hour",
          desc: "小時"
        }
      ],
      formItem: {
        monitors: [],
        monitorTypes: [],
        dateRange: [],
        start: undefined,
        end: undefined
      },
      rules: {
        monitors: [
          {
            required: true,
            type: "array",
            min: 1,
            message: "至少選擇一個測站",
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
        reportUnit: [{ required: true, message: "單位未填", trigger: "blur" }]
      },
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
      this.$refs.realtimeTrend.validate(valid => {
        if (valid) {
          this.query();
        }
      });
    },
    handleReset(name) {
      this.$refs[name].resetFields();
    },
    downloadExcel() {
      let url = baseUrl() + `/Excel/HistoryTrend/${this.query_url}`;
      window.open(url);
    },
    query() {
      this.display = true;
      let monitors = encodeURIComponent(this.formItem.monitors.join(":"));
      let monitorTypes = encodeURIComponent(
        this.formItem.monitorTypes.join(":")
      );

      let end = moment()
        .minutes(0)
        .seconds(0)
        .milliseconds(0)
        .subtract(2, "hours");
      let start = moment(end).subtract(1, "days");

      this.query_url = `${monitors}/${monitorTypes}/${
        this.formItem.reportUnit
      }/${start.valueOf()}/${end.valueOf()}`;

      axios
        .get("/JSON/HistoryTrend/" + this.query_url)
        .then(resp => {
          const ret = resp.data;
          ret.chart = {
            type: "line",
            zoomType: "x",
            panning: true,
            panKey: "shift",
            alignTicks: false
          };

          var pointFormatter = function() {
            var d = new Date(this.x);
            return d.toLocaleString() + ": " + Math.round(this.y) + "度";
          };

          ret.colors = [
            "#7CB5EC",
            "#434348",
            "#90ED7D",
            "#F7A35C",
            "#8085E9",
            "#F15C80",
            "#E4D354",
            "#2B908F",
            "#FB9FA8",
            "#91E8E1",
            "#7CB5EC",
            "#80C535",
            "#969696"
          ];
          ret.tooltip = { valueDecimals: 2 };
          ret.legend = { enabled: true };
          ret.credits = {
            enabled: false,
            href: "http://www.wecc.com.tw/"
          };
          ret.xAxis.type = "datetime";
          ret.xAxis.dateTimeLabelFormats = {
            day: "%b%e日",
            week: "%b%e日",
            month: "%y年%b"
          };

          ret.plotOptions = {
            scatter: {
              tooltip: {
                pointFormatter: pointFormatter
              }
            }
          };

          let myChart = highcharts.chart("reportDiv", ret);
          //$("#downloadExcel").prop("href", "/Excel/HistoryTrend/" + base_url);
          //$("#reportDiv").highcharts(ret);
        })
        .catch(err => {
          alert(err);
        });
    }
  }
};
</script>
