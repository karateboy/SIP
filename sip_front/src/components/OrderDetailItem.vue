<template>
    <div class="modal inmodal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content animated fadeIn">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span
                            aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">新增細項</h4>
                </div>
                <div class="modal-body">
                    <form>
                        <div class="form-group">
                            <label class="col-lg-2 control-label">廢棄物代碼:</label>
                            <div class="col-lg-2"><input type="text" class="form-control"
                                                         v-model="detail.wasteCode"></div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-2 control-label">廢棄物種類:</label>
                            <div class="col-lg-2">{{wasteCodeName}}</div>
                            <div class="col-lg-8">
                                <div class="btn-group" data-toggle="buttons">
                                    <label class="btn btn-outline btn-primary"
                                           v-for="unitOpt in unitList"
                                           @click="detail.unit=unitOpt">
                                        <input type="radio">{{ unitOpt }} </label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-lg-2 control-label">數量:</label>
                            <div class="col-lg-2"><input type="text" class="form-control"
                                                         v-model="order.amount"></div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-white" data-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" @click="addDetail" v-if='opType=="add"'>
                        新增
                    </button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" @click="updateDetail" v-else>
                        更新
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
<style>
    body{
        background-color:#ff0000;
    }


</style>
<script>
    export default{
        props: {
            opType: {
                type: String,
                required: true
            },
            detailIndex: {
                type: Number
            },
            detail: {
                type: Object,
                required: true
            }
        },
        data(){
            return {
                unitList: [
                    '米', '噸'
                ]
            }
        },
        computed:{
            wasteCodeName(){
                return "測試"
            }
        },
        methods: {
            addDetail(){
                this.$emit('addOrderDetail', this.detail)
            },
            updateDetail(){
                this.$emit('updateOrderDetail', {
                    detailIndex:this.detailIndex,
                    detail:this.detail
                })
            }
        },
        components: {}
    }
</script>
