<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group"><label class="col-lg-1 control-label">訂單號碼:</label>
                <div class="col-lg-4"><input type="text" placeholder="訂單號碼" autofocus
                                             class="form-control"
                                             v-model="queryParam._id">
                </div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">品牌:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="品牌"
                                             v-model="queryParam.brand"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">品名:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="品名"
                                             v-model="queryParam.name"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">工廠代號:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="工廠代號"
                                             v-model="queryParam.factoryId"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">客戶編號:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="客戶編號"
                                             v-model="queryParam.customerId"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">出貨日從:</label>
                <div class="col-lg-5">
                    <div class="input-daterange input-group">
                        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                        <datepicker v-model="start" language="zh"
                                    format="yyyy-MM-dd"></datepicker>
                    </div>
                </div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">至(不含):</label>
                <div class="col-lg-5">
                    <div class="input-daterange input-group">
                        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                        <datepicker v-model="end" language="zh"
                                    format="yyyy-MM-dd"></datepicker>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-lg-offset-1">
                    <button class='btn btn-primary' @click='query'>查詢</button>
                </div>
            </div>
        </div>
        <div v-if='display'>
            <div v-if='orderList.length != 0'>
                <order-list :order-list="orderList"></order-list>
            </div>
            <div v-else class="alert alert-info">沒有符合的訂單</div>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
    import axios from 'axios'
    import moment from 'moment'
    import Datepicker from 'vuejs-datepicker'
    import OrderList from "./OrderList.vue"

    export default{
        data(){
            return {
                display: false,
                orderList: [],
                queryParam: {}
            }
        },
        computed: {
            start: {
                get: function () {
                    if (this.queryParam.start)
                        return moment(this.queryParam.start).toDate()
                    else
                        return null
                },
                // setter
                set: function (newValue) {
                    this.queryParam.start = newValue.getTime()
                }
            },
            end: {
                get: function () {
                    if (this.queryParam.end)
                        return moment(this.queryParam.end).toDate()
                    else
                        return null
                },
                // setter
                set: function (newValue) {
                    this.queryParam.end = newValue.getTime()
                }
            }
        },
        methods: {
            prepareParam(){
                if (this.queryParam._id == "")
                    this.queryParam._id = null

                if (this.queryParam.brand == "")
                    this.queryParam.brand = null

                if (this.queryParam.name == "")
                    this.queryParam.name = null

                if (this.queryParam.factoryId == '')
                    this.queryParam.factoryId = null

                if (this.queryParam.customerId == '')
                    this.queryParam.customerId = null
            },
            query(){
                this.prepareParam()
                axios.post('/QueryOrder', this.queryParam).then((resp) => {
                    const ret = resp.data
                    this.orderList.splice(0, this.orderList.length)
                    for (let order of ret) {
                        this.orderList.push(order)
                    }
                    this.display = true
                }).catch((err) => {
                    alert(err)
                })
            }
        },
        components: {
            OrderList,
            Datepicker
        }
    }
</script>
