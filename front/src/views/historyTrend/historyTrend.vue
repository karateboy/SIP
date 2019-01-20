<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Col>
                <Card>
                    <Form ref="historyTrend" :model="formItem" :rules="rules" :label-width="80">
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
                        <FormItem label="圖表類型" prop="chartType">
                            <Select v-model="formItem.chartType" filterable>
                                <Option v-for="chart in chartType" :value="chart.type" :key="chart.type">{{ chart.desc }}</Option>
                            </Select>
                        </FormItem>
                        
                        <FormItem label="資料區間" prop="dateRange">
                                <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm" 
                                    placeholder="選擇資料區間" style="width: 300px"
                                    v-model="formItem.dateRange"></DatePicker>
                        </FormItem>
                        <FormItem>
                            <Button type="primary" @click="handleSubmit">查詢</Button>
                            <Button type="ghost" style="margin-left: 8px" @click="handleReset('historyTrend')">取消</Button>
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
.normal {
  color: black;
}

.over_internal_std {
  color: DarkBlue;
}

.over_law_std {
  color: DarkRed;
}

.calibration_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#b4e391+0,61c419+50,b4e391+100;Green+3D */
  background: #b4e391; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#b4e391', endColorstr='#b4e391',GradientType=0 ); /* IE6-9 */
}
.maintain_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#e570e7+0,c85ec7+47,a849a3+100;Pink+3D+%233 */
  background: #e570e7; /* Old browsers */
  background: -moz-linear-gradient(top, #e570e7 0%, #c85ec7 47%, #a849a3 100%);
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #e570e7 0%,
    #c85ec7 47%,
    #a849a3 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #e570e7 0%,
    #c85ec7 47%,
    #a849a3 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#e570e7', endColorstr='#a849a3',GradientType=0 ); /* IE6-9 */
}
.abnormal_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#ff3019+0,cf0404+100;Red+3D */
  background: #ff3019; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #ff3019 0%,
    #cf0404 100%
  ); /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #ff3019 0%,
    #cf0404 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #ff3019 0%,
    #cf0404 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ff3019', endColorstr='#cf0404',GradientType=0 ); /* IE6-9 */
}
.auto_audit_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#d0e4f7+0,73b1e7+24,0a77d5+50,539fe1+79,87bcea+100;Blue+Pipe+%231 */
  background: #d0e4f7; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  );
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  );
  /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#d0e4f7', endColorstr='#87bcea',GradientType=0 );
  /* IE6-9 */
}
.manual_audit_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#f1e767+0,feb645+100;Yellow+3D */
  background: #f1e767; /* Old browsers */
  background: -moz-linear-gradient(top, #f1e767 0%, #feb645 100%);
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #f1e767 0%,
    #feb645 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #f1e767 0%,
    #feb645 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#f1e767', endColorstr='#feb645',GradientType=0 ); /* IE6-9 */
}
</style>
<script>
import highcharts from "highcharts";
import highchart_more from "highcharts/highcharts-more";
import exporting from "highcharts/modules/exporting";
import Cookies from "js-cookie";
import axios from "axios";
import moment from "moment";
import baseUrl from "../../baseUrl";
export default {
  name: "historyTrend",

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
          id: "TenMin",
          desc: "十分"
        },
        {
          id: "Hour",
          desc: "小時"
        },
        {
          id: "Day",
          desc: "日"
        },
        {
          id: "Month",
          desc: "月"
        },
        {
          id: "Quarter",
          desc: "季"
        },
        {
          id: "Year",
          desc: "年"
        }
      ],
      chartType: [
        {
          type: "line",
          desc: "折線圖"
        },
        {
          type: "spline",
          desc: "曲線圖"
        },
        {
          type: "area",
          desc: "面積圖"
        },
        {
          type: "areaspline",
          desc: "曲線面積圖"
        },
        {
          type: "column",
          desc: "柱狀圖"
        },
        {
          type: "scatter",
          desc: "點圖"
        },
        {
          type: "boxplot",
          desc: "盒鬚圖"
        }
      ],
      formItem: {
        monitors: [],
        monitorTypes: [],
        dateRange: [],
        chartType: "line",
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
        reportUnit: [{ required: true, message: "單位未填", trigger: "blur" }],
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
      this.$refs.historyTrend.validate(valid => {
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

      if (this.formItem.chartType === "boxplot") {
        if (this.formItem.monitorTypes.length > 1) {
          alert("盒鬚圖只能選擇單一測項!");
          return;
        }
      }
      let start = this.formItem.dateRange[0].getTime();
      let end = this.formItem.dateRange[1].getTime();
      this.query_url = `${monitors}/${monitorTypes}/${
        this.formItem.reportUnit
      }/${start}/${end}`;

      let url_head = "";
      if (this.formItem.chartType !== "boxplot")
        url_head = "/JSON/HistoryTrend/";
      else url_head = "/JSON/HistoryBoxplot/";

      axios
        .get(url_head + this.query_url)
        .then(resp => {
          const ret = resp.data;
          if (this.formItem.chartType !== "boxplot") {
            ret.chart = {
              type: this.formItem.chartType,
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
          } else {
            //extra setting for boxplot
            console.log(ret);
          }
          highchart_more(highcharts);
          exporting(highcharts);
          highcharts.setOptions({
            global: {
              useUTC: false
            },
            lang: {
              contextButtonTitle: "圖表功能表",
              downloadJPEG: "下載JPEG",
              downloadPDF: "下載PDF",
              downloadPNG: "下載PNG",
              downloadSVG: "下載SVG",
              drillUpText: "回到{series.name}.",
              noData: "無資料",
              months: [
                "1月",
                "2月",
                "3月",
                "4月",
                "5月",
                "6月",
                "7月",
                "8月",
                "9月",
                "10月",
                "11月",
                "12月"
              ],
              printChart: "列印圖表",
              resetZoom: "重設放大區間",
              resetZoomTitle: "回到原圖大小",
              shortMonths: [
                "1月",
                "2月",
                "3月",
                "4月",
                "5月",
                "6月",
                "7月",
                "8月",
                "9月",
                "10月",
                "11月",
                "12月"
              ],
              weekdays: [
                "星期日",
                "星期一",
                "星期二",
                "星期三",
                "星期四",
                "星期五",
                "星期六"
              ]
            }
          });
          let myChart = highcharts.chart("reportDiv", ret);
        })
        .catch(err => {
          alert(err);
        });
    }
  }
};
</script>
