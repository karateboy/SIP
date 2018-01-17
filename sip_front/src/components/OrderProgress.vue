<template>
    <div>
        <table class="table table-hover table-bordered table-condensed">
            <thead>
            <tr>
                <th>顏色</th>
                <th>尺寸</th>
                <th>數量(打)</th>
                <th>完成</th>
                <th>進度(打): (生產中/已完成)</th>
                <th>生產百分比</th>
                <th>耗損(雙)</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for='(detail, idx) in order.details'>
                <td>{{detail.color}}</td>
                <td>{{detail.size}}</td>
                <td>{{ showDozen(detail.quantity)}}</td>
                <td>
                    <i class="fa fa-check" aria-hidden="true" style="color:green" v-if='detail.complete'></i>
                    <i class="fa fa-times" style="color:red" aria-hidden="true" v-else></i>
                </td>
                <td>
                    <div v-if='productionSummary[idx]'>
                        <span class='text-warning'>{{ showDozen(productionSummary[idx].inProduction)}}</span>/
                        <span class='text-success'>{{ showDozen(productionSummary[idx].finished)}}</span>
                    </div>
                </td>
                <td>
                    <div class="progress">
                        <div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar"
                             :aria-valuenow="productionPercent(idx)" aria-valuemin="0" aria-valuemax="100" :style="{width:productionPercent(idx)+'%'}">
                            {{productionPercent(idx)}}%
                        </div>
                    </div>
                </td>
                <td>
                    <div v-if='productionSummary[idx]'>
                        {{productionSummary[idx].overhead}}
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</template>
<style>
</style>
<script>
    import {mapGetters} from 'vuex'
    import axios from 'axios'
    import * as dozenExpr from '../dozenExp'

    export default{
        data(){
            return {
                productionSummary_: []
            }
        },
        computed: {
            ...mapGetters(['order']),
            productionSummary(){
                this.productionSummary_.splice(0, this.productionSummary_.length)
                this.order.details.forEach((detail, idx) => {
                    let summary = {
                        inProduction: 0,
                        finished: 0,
                        overhead: 0
                    }

                    this.productionSummary_.push(summary)
                    axios.get("/OrderWorkCard/" + this.order._id + '/' + idx).then((resp) => {
                        const ret = resp.data
                        summary.nWorkCard = ret.length
                        for (let workCard of ret) {
                            if (workCard.active) {
                                summary.inProduction += workCard.good
                            } else {
                                summary.finished += workCard.good
                            }
                            summary.overhead += (workCard.quantity - workCard.good)
                        }
                    }).catch((err) => {
                        alert(err)
                    })
                })
                return this.productionSummary_
            }
        },
        methods: {
            productionPercent(idx){
                let percent = (this.productionSummary_[idx].inProduction + this.productionSummary_[idx].finished)*100/this.order.details[idx].quantity
                return parseInt(percent)
            },
            showDozen(v){
                return dozenExpr.toDozenStr(v)
            }
        },
        components: {}
    }
</script>
