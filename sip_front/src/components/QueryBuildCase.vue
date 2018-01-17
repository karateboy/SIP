<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-1 control-label">起造人案名:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="起造人" v-model="queryParam.name"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">建築師:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="" v-model="queryParam.architect"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">縣市:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="新北市" v-model="queryParam.county"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">地號:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="地址" v-model="queryParam.addr"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">樓板面積(大於):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="queryParam.areaGT"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">樓板面積(小於):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="queryParam.areaLT"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">黃色警報:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="queryParam.yellowAlert">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">紅色警報:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="queryParam.redAlert">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">是否簽約:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="queryParam.contracted">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">業務:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="業務" v-model="queryParam.sales"></div>
            </div>

            <div class="form-group">
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='query'>查詢</button>
                </div>
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='queryMap'>顯示地圖</button>
                </div>
                <div v-if="user.groupId == 'Admin'" class="col-lg-1 col-lg-offset-1">
                    <button class="btn btn-info" @click='exportExcel'>下載Excel</button>
                </div>
            </div>
        </div>
        <div v-if='display'>
            <build-case-list url="/QueryBuildCase" :param="queryParam"></build-case-list>
        </div>
        <div v-if='showMap'>
            <build-case-map url="/QueryBuildCase" :param="queryParam"></build-case-map>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
import axios from 'axios'
import moment from 'moment'
import { mapGetters } from 'vuex'
import baseUrl from "../baseUrl"

import BuildCaseList from "./BuildCaseList.vue"
import BuildCaseMap from "./BuildCaseMap.vue"

export default {
    data() {
        return {
            display: false,
            showMap: false,
            queryParam: {
                yellowAlert: true,
                redAlert: false,
            }
        }
    },
    computed: {
        ...mapGetters(['user'])
    },
    methods: {
        query() {
            if (!this.display)
                this.display = true

            this.queryParam = Object.assign({}, this.queryParam)
        },
        queryMap() {
            if (!this.showMap)
                this.showMap = true

            this.queryParam = Object.assign({}, this.queryParam)
        },
        exportExcel() {
            let json = JSON.stringify(this.queryParam)
            let segment = encodeURIComponent(json)
            let url = baseUrl() + "/BuildCase/" + segment
            window.open(url)
        }
    },
    components: {
        BuildCaseList,
        BuildCaseMap
    }
}
</script>
