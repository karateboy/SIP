<template>
    <div>
        <div class="ibox-content">
            <div class="form-horizontal">
                <div class="form-group has-feedback"><label class="col-lg-1 control-label">訂單號碼:</label>
                    <div class="col-lg-4"><input type="text" placeholder="訂單號碼" autofocus
                                                 class="form-control"
                                                 v-model="order._id" :readonly='!isNewOrder'>
                     </div>
                </div>
                <div class="form-group"><label class="col-lg-1 control-label">聯絡人:</label>
                    <div class="col-lg-4"><input type="text" class="form-control" v-model="order.contact"></div>
                </div>
                <div class="form-group"><label class="col-lg-1 control-label">通報日期:</label>
                    <div class='col-lg-4'>
                        <datepicker v-model="notifiedDate" language="zh"
                                    format="yyyy-MM-dd"></datepicker>
                    </div>
                </div>
                <div class="form-group"><label class="col-lg-1 control-label">訂單總量:</label>
                    <div class="col-lg-4"><input type="number" class="form-control" v-model="quantity" readonly>
                    </div>
                </div>
                <order-detail-item id="detailModal" :opType='detailOpType' :detailIndex='detailIndex'
                                   :detail='getDetailItem()'
                                   @addOrderDetail='addDetailItem'
                                   @updateOrderDetail='updateDetailItem'
                ></order-detail-item>

                <div class="form-group">
                    <label class="col-lg-1 control-label">清運細項:</label>
                    <div class="col-lg-4">
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th>清運代碼</th>
                                <th>廢棄物種類</th>
                                <th>單位</th>
                                <th>數量</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="(detail, idx) in details">
                                <td>{{detail.wasteCode}}</td>
                                <td>{{detail.wasteCode}}</td>
                                <td>{{detail.unit}}</td>
                                <td>{{detail.quantity}}</td>
                                <td>
                                    <button class="btn btn-danger" @click="delDetail(idx)" v-if='isNewOrder'>
                                        <i class="fa fa-trash" aria-hidden="true"></i>&nbsp;刪除
                                    </button>
                                    <button class="btn btn-warning" @click="editDetail(idx)" data-toggle="modal" data-target="#detailModal">
                                        <i class="fa fa-pencil" aria-hidden="true"></i>&nbsp;更新
                                    </button>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#detailModal" @click="detailOpType='add'">
                        <i class="fa fa-plus" aria-hidden="true"></i>&nbsp;新增
                    </button>
                </div>

                <div class="form-group">
                    <div v-if='isNewOrder' class="col-lg-offset-1">
                        <button class="btn btn-primary" :class="{disabled: !readyForSubmit}"
                                @click.prevent="upsertOrder" :disabled="!readyForSubmit">新增
                        </button>
                    </div>
                    <div v-else class="col-lg-offset-1">
                        <button class="btn btn-primary" :class="{disabled: !readyForSubmit}"
                                @click.prevent="upsertOrder" :disabled="!readyForSubmit">更新
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>
<style scoped>
    body{
        background-color:#0000ff;
    }
</style>
<script>
    import {mapGetters} from 'vuex'
    import axios from 'axios'
    import moment from 'moment'
    import Datepicker from 'vuejs-datepicker'
    import * as dozenExp from '../dozenExp'
    import OrderDetailItem from './OrderDetailItem.vue'

    export default{
        data(){
            return {
                detail: {
                    color: "",
                    size: "",
                    dozenNumber: 1,
                    quantity: 12,
                    complete: false
                },
                detailOpType: "add",
                detailIndex: 0
            }
        },
        computed: {
            ...mapGetters(['user', 'order', 'isNewOrder']),
            salesName(){
                return "";
            },
            details(){
              if(this.order.contract && this.order.contact.details)
                  return this.order.contact.details
                else
                    return []
            },
            quantity(){
                var sum = 0;
                for (var detail of this.details) {
                    sum += detail.quantity
                }
                return sum
            },
            readyForSubmit(){
                if (this.order._id === ""
                        || this.order.name === ""
                        || this.order.expectedDeliverDate === ""
                        )
                    return false;
                else
                    return true;
            },
            notifiedDate(){
              return new Date(this.order.notifiedDate)
            },
            expectedDeliverDate: {
                get: function () {
                    if (this.order.expectedDeliverDate)
                        return new Date(this.order.expectedDeliverDate)
                    else {
                        const deliver = moment("0", "hh").add(1, 'month').toDate()
                        this.order.expectedDeliverDate = deliver.getTime()
                        return deliver
                    }
                },
                // setter
                set: function (newValue) {
                    this.order.expectedDeliverDate = newValue.getTime()
                }
            }
        },
        watch: {
            "order._id": function (newId) {
                if (!this.isNewOrder)
                    return

                if (newId.trim() != "") {
                    const url = "/checkOrderId/" + newId
                    axios.get(url).then(
                            (resp) => {
                                const data = resp.data
                                this.isOrderIdOkay = data.ok
                            }
                    )
                } else
                    this.isOrderIdOkay = false
            }
        },
        methods: {
            prepareOrder(){
                if (!this.order.salesId)
                    this.order.salesId = this.user._id;

                if (this.order.packageInfo.numInBag == "")
                    this.order.packageInfo.numInBag = null
            },
            upsertOrder(){
                this.prepareOrder();
                axios.post("/Order", this.order).then(
                        (resp) => {
                            const data = resp.data
                            if (data.ok) {
                                alert("成功")
                                this.$router.push({name: 'MyOrder'})
                            }
                            else
                                alert("失敗:" + data.msg)
                        }
                ).catch((err) => {
                    alert(err);
                })
            },
            getDetailItem(){
                if (this.detailOpType === 'add')
                    return this.detail
                else
                    return this.order.details[this.detailIndex]
            },
            addDetailItem(detail){
                var copy = Object.assign({}, detail);
                this.order.details.push(copy);
            },
            updateDetailItem(evt){
              console.log(evt)
            },
            addDetail(){
                this.detail.quantity = this.detail.dozenNumber * 12;
                var copy = Object.assign({}, this.detail);
                this.order.details.push(copy);
            },
            delDetail(idx){
                this.order.details.splice(idx, 1)
            },
            editDetail(idx){
              this.detailOpType = 'edit'
                this.detailIndex = idx
            },
            detailQuantity(idx){
                return dozenExp.toDozenStr(this.order.details[idx].quantity)
            },
            getDozenQuantity(newValue){
                return dozenExp.fromDozenStr(newValue)
            }
        },
        components: {
            Datepicker,
            OrderDetailItem
        }
    }
</script>
