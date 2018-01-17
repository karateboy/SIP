<template>
    <div>
        <div v-if='unhandledOrder.length != 0'>
            <order-list :order-list="unhandledOrder"></order-list>
        </div>
        <div v-else class="alert alert-info" role="alert"> 沒有等待處理通報</div>

    </div>
</template>
<style>
    body {
    }


</style>
<script>
    import OrderList from './OrderList.vue'
    import {mapGetters} from 'vuex'
    import axios from 'axios'

    export default{
        data(){
            return {
                fetched: false,
                orderList: []
            }
        },
        computed: {
            ...mapGetters(['user']),
            unhandledOrder(){
                if (!this.fetched) {
                    const url = "/UnhandledOrder"
                    axios.get(url).then(
                        (resp) => {
                            const len = this.orderList.length
                            this.orderList.splice(0, len)
                            for (let v of resp.data) {
                                this.orderList.push(v)
                            }
                        }
                    ).catch((err) => {
                        alert(err)
                    })
                    this.fetched = true
                }
                return this.orderList
            }
        },
        methods: {},
        components: {
            OrderList
        }
    }
</script>
