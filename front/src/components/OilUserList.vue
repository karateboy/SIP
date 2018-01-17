<template>
    <div>
        <div v-if="oilUserList.length != 0">
            <table class="table table-hover table-bordered table-condensed">
                <thead>
                    <tr class='info'>
                        <th></th>
                        <th>類型</th>
                        <th>縣市</th>
                        <th>名稱</th>
                        <th>地址</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(oilUser, index) in oilUserList" :class='{success: selectedIndex == index}'>
                        <td>
                            <button class="btn btn-primary" @click="editOilUser(index)">
                                <i class="fa fa-pen"></i>&nbsp;編輯</button>
                        </td>
                        <td>{{ displayUseType(oilUser)}}</td>
                        <td>{{ oilUser.county}}</td>
                        <td>{{ oilUser.name}}</td>
                        <td>{{ oilUser.addr}}</td>
                    </tr>
                </tbody>
            </table>
            <pagination for="cardList" :records="total" :per-page="5" count-text="第{from}到第{to}筆/共{count}筆|{count} 筆|1筆"></pagination>
        </div>
        <div v-else class="alert alert-info" role="alert">沒有符合的機構</div>
        <oil-user-detail v-if="display === 'detail'" :oilUser="oilUserList[selectedIndex]"></oil-user-detail>
    </div>
</template>
<style scoped>

</style>
<script>
import axios from 'axios'
import moment from 'moment'
import { Pagination, PaginationEvent } from 'vue-pagination-2'
import OilUserDetail from './OilUserDetail.vue'

export default {
    props: {
        url: {
            type: String,
            required: true
        },
        param: {
            type: Object
        }
    },
    data() {
        return {
            oilUserList: [],
            limit: 5,
            total: 0,
            display: "",
            selectedIndex: -1
        }
    },
    mounted: function() {
        this.fetchOilUser(0, this.limit)
        PaginationEvent.$on('vue-pagination::cardList', this.handlePageChange)
    },
    watch: {
        url: function(newUrl) {
            this.fetchOilUser(0, this.limit)
        },
        param: function(newParam) {
            this.fetchOilUser(0, this.limit)
        }
    },

    methods: {
        processResp(resp) {
            const ret = resp.data
            this.oilUserList.splice(0, this.oilUserList.length)

            for (let oilUser of ret) {
                this.oilUserList.push(oilUser)
            }
        },
        fetchOilUser(skip, limit) {
            let request_url = `${this.url}/${skip}/${limit}`

            if (this.param) {
                axios.post(request_url, this.param).then(this.processResp).catch((err) => {
                    alert(err)
                })
            } else {
                axios.get(request_url).then(this.processResp).catch((err) => {
                    alert(err)
                })
            }
            this.fetchOilUserCount()
        },
        fetchOilUserCount() {
            let request_url = `${this.url}/count`
            if (this.param) {
                axios.post(request_url, this.param).then(resp => {
                    this.total = resp.data
                }).catch((err) => {
                    alert(err)
                })
            } else {
                axios.get(request_url).then(resp => {
                    this.total = resp.data
                }).catch((err) => {
                    alert(err)
                })
            }
        },
        handlePageChange(page) {
            let skip = (page - 1) * this.limit
            this.fetchOilUser(skip, this.limit)
        },
        editOilUser(idx) {
            this.selectedIndex = idx
            this.display = 'detail';
        },
        displayUseType(oilUser){
            if(oilUser.useType == "gasStation")
                return "加油站"
            else if(oilUser.useType == "tank")
                return "油槽"
            else
                return "鍋爐"
        }
    },
    components: {
        Pagination,
        OilUserDetail
    }
}
</script>
