<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group"><label class="col-lg-1 control-label">公立:</label>
                <div class="col-lg-4"><input type="checkbox"
                                             class="form-control"
                                             v-model="queryParam.isPublic">
                </div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">有座標:</label>
                <div class="col-lg-4"><input type="checkbox"
                                             class="form-control"
                                             v-model="queryParam.hasLocation">
                </div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">機構名稱:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="養護中心"
                                             v-model="queryParam.name"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">負責人:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder=""
                                             v-model="queryParam.principal"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">區域:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="區域"
                                             v-model="queryParam.district"></div>
            </div>
            <div class="form-group"><label class="col-lg-1 control-label">地址:</label>
                <div class="col-lg-4"><input type="text" class="form-control" placeholder="地址"
                                             v-model="queryParam.addr"></div>
            </div>
            <div class="form-group">
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='query'>查詢</button>
                </div>
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='queryMap'>顯示地圖</button>
                </div>
                <!--                <div class="col-lg-1 col-lg-offset-1">
                                    <button class="btn btn-info" @click='exportExcel'>Excel</button>
                                </div>-->
            </div>
        </div>
        <div v-if='display'>
            <care-house-list url="/QueryCareHouse" :param="queryParam"></care-house-list>
        </div>
        <div v-if='showMap'>
            <care-house-map url="/QueryCareHouse" :param="queryParam"></care-house-map>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
    import axios from 'axios'
    import moment from 'moment'
    import CareHouseList from "./CareHouseList.vue"
    import CareHouseMap from "./CareHouseMap.vue"

    export default {
        data() {
            return {
                display: false,
                showMap: false,
                queryParam: {
                    hasLocation: true
                }
            }
        },
        methods: {
            prepareParam() {
                if (this.queryParam.brand == "")
                    this.queryParam.brand = null

                if (this.queryParam.name == "")
                    this.queryParam.name = null

                if (this.queryParam.principal == "")
                    this.queryParam.principal = null

                if (this.queryParam.district == '')
                    this.queryParam.district = null
            },
            query() {
                this.prepareParam()
                if (!this.display)
                    this.display = true

                this.queryParam = Object.assign({}, this.queryParam)
            },
            queryMap(){
                this.prepareParam()
                if (!this.showMap)
                    this.showMap = true

                this.queryParam = Object.assign({}, this.queryParam)
            }
        },
        components: {
            CareHouseList,
            CareHouseMap
        }
    }
</script>
