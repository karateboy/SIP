<template>
    <div>
        <div v-if="careHouseList.length != 0">
            <table class="table table-hover table-bordered table-condensed">
                <thead>
                <tr class='info'>
                    <th></th>
                    <th>公立</th>
                    <th>機構名稱</th>
                    <th>負責人</th>
                    <th>區域</th>
                    <th>地址</th>
                    <th>電話</th>
                    <th>安養</th>
                    <th>養護</th>
                    <th>長照</th>
                    <th>差兩管床數</th>
                    <th>廢棄物</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(careHouse, index) in careHouseList" :class='{success: selectedIndex == index}'>
                    <td>
                        <button class="btn btn-primary" @click="editCareHouse(index)"><i class="fa fa-pen"></i>&nbsp;編輯</button>
                    
                    </td>
                    <td>{{ displayIsPublic(careHouse.isPublic)}}</td>
                    <td>{{ careHouse.name}}</td>
                    <td>{{ careHouse.principal}}</td>
                    <td>{{ careHouse.district}}</td>
                    <td>{{ careHouse.addr}}</td>
                    <td>{{ careHouse.phone}}</td>
                    <td>{{ displayBed("安養", careHouse) }}</td>
                    <td>{{ displayBed("養護", careHouse) }}</td>
                    <td>{{ displayBed("長照", careHouse) }}</td>
                    <td>{{ careHouse.beds}}</td>
                    <td>{{ careHouse.waste}}</td>
                </tr>
                </tbody>
            </table>
            <pagination for="cardList" :records="total" :per-page="5"
                        count-text="第{from}到第{to}筆/共{count}筆|{count} 筆|1筆"></pagination>
        </div>
        <div v-else class="alert alert-info" role="alert">沒有符合的機構</div>
        <care-house-detail v-if="display === 'detail'" :careHouse="careHouseList[selectedIndex]"></care-house-detail>
    </div>
</template>
<style scoped>
    body {
    }
</style>
<script>
    import axios from 'axios'
    import {Pagination, PaginationEvent} from 'vue-pagination-2'
    import CareHouseDetail from './CareHouseDetail.vue'

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
                careHouseList: [],
                limit: 5,
                total: 0,
                display: "",
                selectedIndex: -1
            }
        },
        mounted: function () {
            this.fetchCareHouse(0, this.limit)
            PaginationEvent.$on('vue-pagination::cardList', this.handlePageChange)
        },
        watch: {
            url: function (newUrl) {
                this.fetchCareHouse(0, this.limit)
            },
            param: function (newParam) {
                this.fetchCareHouse(0, this.limit)
            }
        },

        methods: {
            processResp(resp) {
                const ret = resp.data
                this.careHouseList.splice(0, this.careHouseList.length)

                for (let careHouse of ret) {
                    this.careHouseList.push(careHouse)
                }
            },
            fetchCareHouse(skip, limit) {
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
                this.fetchCareHouseCount()
            },
            fetchCareHouseCount() {
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
                this.fetchCareHouse(skip, this.limit)
            },
            displayBed(careTypeName, careHouse) {
                for (let careType of careHouse.careTypes) {
                    if (careType.name === careTypeName)
                        return careType.quantity
                }
                return 0
            },
            displayIsPublic(v) {
                if (v) return "公立"
                else return "私立"
            },
            editCareHouse(idx) {
                this.selectedIndex = idx
                this.display = 'detail';
            }
        },
        components: {
            Pagination,
            CareHouseDetail
        }
    }
</script>
