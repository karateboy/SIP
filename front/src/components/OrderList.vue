<template>
    <div>
        <table class="table table-hover table-bordered table-condensed">
            <thead>
            <tr class='info'>
                <th></th>
                <th>訂單編號</th>
                <th>聯絡人</th>
                <th>地址</th>
                <th>電話</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(order, index) in myList" :class='{success: selectedIndex == index}'>
                <td>
                    <button class="btn btn-primary" @click="displayOrder(index)"><i class="fa fa-eye"></i>&nbsp;內容</button>
                    <button class="btn btn-primary" @click='displayProgress(index)'><i class="fa fa-truck" aria-hidden="true"></i>&nbsp;進度</button>
                    <button class="btn btn-info" @click='getPdf(index)'><i class="fa fa-pdf" aria-hidden="true"></i>&nbsp;列印</button>
                    <button class="btn btn-success" @click="closeOrder(index)" v-if='order.active'><i class="fa fa-money"></i>&nbsp;結案</button>
                    <button class="btn btn-danger" @click="deleteOrder(index)" v-if='order.active'><i class="fa fa-trash"></i>&nbsp;刪除</button>

                </td>
                <td>{{ order._id}}</td>
                <td>{{ order.contact}}</td>
                <td>{{ order.address}}</td>
                <td>{{ order.phone}}</td>
            </tr>
            </tbody>
        </table>
        <hr>
        <div v-if="display=='detail'">
            <order-detail></order-detail>
        </div>
        <div v-else-if="display=='progress'">
            <order-progress></order-progress>
        </div>
    </div>
</template>
<style scoped>
    body{
    }


</style>
<script>
    import {mapActions} from 'vuex'
    import OrderDetail from './OrderDetail.vue'
    import OrderProgress from './OrderProgress.vue'
    import moment from 'moment'
    import baseUrl from '../baseUrl'
    import {toDozenStr} from '../dozenExp'
    import axios from 'axios'
    export default{
        props: {
            orderList: {
                type: Array,
                required: true
            }
        },
        data(){
            return {
                display: '',
                selectedIndex: -1,
                order: {}
            }
        },
        computed: {
            myList(){
                return this.orderList;
            }
        },
        methods: {
            ...mapActions(['showOrder', 'cloneOrder']),
            displayDate(millis){
                const mm = moment(millis)
                const dateStr = mm.format('YYYY-MM-DD')
                const afterStr = mm.fromNow()
                return dateStr + " (" + afterStr + ")";
            },
            displayOrder(idx){
                this.selectedIndex = idx
                this.showOrder(this.myList[idx])
                this.display = 'detail';
            },
            prepareCloneOrder(idx){
                this.selectedIndex = idx
                this.cloneOrder(this.myList[idx])
                this.$router.push({name: 'NewOrder'})
            },
            displayProgress(idx){
                this.selectedIndex = idx
                this.showOrder(this.myList[idx])
                this.display = 'progress'
            },
            getPdf(idx){
                let url = baseUrl() + "/OrderPDF/" + this.myList[idx]._id
                window.open(url)
            },
            closeOrder(idx){
                axios.post("/CloseOrder/"+ this.orderList[idx]._id).then((resp)=>{
                    const ret = resp.data
                    if(ret.ok){
                        alert("訂單結案")
                        this.orderList.splice(idx, 1)
                    }else{
                        alert(ret.msg)
                    }
                }).catch((err)=>{
                    alert(err)
                })
            },
            deleteOrder(idx){
                axios.delete("/Order/"+ this.orderList[idx]._id).then((resp)=>{
                    const ret = resp.data
                    if(ret.ok){
                        alert("訂單刪除")
                        this.orderList.splice(idx, 1)
                    }else{
                        alert(ret.msg)
                    }
                }).catch((err)=>{
                    alert(err)
                })
            },
            displayQuantity(order){
                let total =0;
                for(let detail of order.details){
                    total += detail.quantity
                }
                return toDozenStr(total)
            }
        },
        components: {
            OrderDetail,
            OrderProgress
        }
    }
</script>
