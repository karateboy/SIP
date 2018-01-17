<template>
    <div>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-2 control-label">登入帳號</label>
                <div class="col-lg-10">
                    <input type="text" placeholder="帳號" class="form-control"
                           required v-model="user._id">
                </div>
            </div>
            <div class="form-group"><label class="col-lg-2 control-label">密碼</label>
                <div class="col-lg-5">
                    <input type="password" placeholder="密碼" class="form-control"
                           required v-model="user.password">
                </div>
            </div>
            <div class="form-group"><label class="col-lg-2 control-label">密碼再輸入</label>
                <div class="col-lg-5">
                    <input type="password" class="form-control"
                           required v-model="user.passwordRetype" >
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-2 control-label">顯示名稱</label>
                <div class="col-lg-5">
                    <input type="text" class="form-control"
                           required v-model="user.name">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-2 control-label">行動電話</label>
                <div class="col-lg-5">
                    <input name='phone' type="tel" placeholder="電話號碼" class="form-control"
                           required v-model="user.phone">
                </div>
            </div>
            <div class="form-group">
                <label class="col-lg-2 control-label">群組:</label>
                <div class="col-lg-7">
                    <div class="btn-group" data-toggle="buttons">
                        <label class="btn btn-outline btn-primary dim"
                               v-for="group in groupInfoList"
                               @click="user.groupId=group.id"
                        :class="{active: user.groupId==group.id }">
                            <input type="radio">{{ group.name }} </label>
                    </div>
                </div>
            </div>
            <div class="form-group" v-if='isNew'>
                <div class="col-lg-offset-2 col-lg-10">
                    <button class="btn btn-primary" @click.prevent="newUser">新增</button>
                </div>
            </div>
            <div class="form-group" v-else>
                <div class="col-lg-offset-2 col-lg-10">
                    <button class="btn btn-primary" @click.prevent="updateUser">更新</button>
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
    import axios from 'axios'
    import { mapActions } from 'vuex'

    export default{
        props:{
            user:{
                type:Object,
                required: true
            },
            isNew:{
                type:Boolean,
                required: true
            }
        },
        data(){
            axios.get("/Group").then((resp) => {
                const ret = resp.data
                this.groupInfoList.splice(0, this.groupInfoList.length)
                for (let group of ret) {
                    this.groupInfoList.push(group)
                }
            })
            return{
                groupInfoList: []
            }
        },
        computed:{
            passwordDifferent(){
                return this.user.password != this.user.passwordRetype
            }
        },
        methods:{
                ...mapActions(['logout']),
            newUser(){
                if(this.user.password != this.user.passwordRetype){
                    alert('密碼不一致')
                    return
                }


                axios.post('/User', this.user).then((resp)=>{
                    const ret = resp.data
                    if(ret.ok)
                        alert("成功")
                    else
                        alert("失敗:" + ret.msg)
                }).catch((err)=>{
                    alert(err)
                })
            },
            updateUser(){
                if(this.user.password != this.user.passwordRetype){
                    alert('密碼不一致')
                    return
                }


                axios.put('/User/'+this.user._id, this.user).then((resp)=>{
                    const ret = resp.data
                    if(ret.ok){
                        alert("成功")
                    }
                    else
                        alert("失敗:" + ret.msg)
                }).catch((err)=>{
                    alert(err)
                })
            }
        },
        components:{
        }
    }
</script>
