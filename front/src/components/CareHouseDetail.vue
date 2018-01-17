<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-1 control-label">公立:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="careHouse.isPublic">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">縣市:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.county"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">機構名稱:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.name"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">負責人:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.principal"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">區域:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.district"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">地址:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.addr"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">電話:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.phone"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">床數:</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="careHouse.beds"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">廢棄物:</label>
                <div class="col-lg-4"><input type="text" class="form-control" v-model="careHouse.waste"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">廢棄物數量(噸/月):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="careHouse.monthlyWaste"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">處理預算(含感染廢證明):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="careHouse.budget"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">尿布用量(大):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="careHouse.diaperBig"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">尿布用量(小):</label>
                <div class="col-lg-4"><input type="number" class="form-control" v-model.number="careHouse.diaperSmall"></div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">簽約:</label>
                <div class="col-lg-4"><input type="checkbox" class="form-control" v-model="careHouse.contracted">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-1 control-label">最後訪談日:</label>
                <div class="col-lg-4"><input type="date" class="form-control" v-model="lastVisit"></div>
            </div>
            <div class="form-group">
                <div class="col-lg-1 col-lg-offset-1">
                    <button class='btn btn-primary' @click='save'>更新</button>
                </div>
            </div>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
import axios from 'axios'
import moment from 'moment'

export default {
    props: {
        careHouse: {
            type: Object,
            required: true
        }
    },
    computed: {
        lastVisit: {
            get() {
                if (this.careHouse.lastVisit) {
                    const date = new moment(this.careHouse.lastVisit)
                    return date.format('YYYY-MM-DD')
                }else
                    return undefined
            },
            set(v) {
                this.careHouse.lastVisit = v
            }
        }
    },
    methods: {
        save() {
            axios.put("/CareHouse", this.careHouse).then(
                resp => {
                    let ret = resp.data
                    if (ret.Ok) {
                        alert("成功!")
                    }
                }

            ).catch(err => alert(err))
        }
    },
    components: {
    }
}
</script>
