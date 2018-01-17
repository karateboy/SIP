<template>
    <div>
        <br>
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-2 control-label">使用者:</label>
                <div class="col-lg-7">
                    <div class="btn-group" data-toggle="buttons">
                        <label class="btn btn-outline btn-primary dim"
                               v-for="user in userList"
                               @click="userId=user._id">
                            <input type="radio">{{ user.name }} </label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-lg-offset-2 col-lg-10">
                    <button class="btn btn-primary" @click.prevent="delUser">刪除</button>
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
    export default{
        data(){
            this.refresh()
            return {
                userList: [],
                userId: ""
            }
        },
        methods: {
            delUser(){
                axios.delete('/User/' + this.userId).then((resp) => {
                    const ret = resp.data
                    if (ret.ok) {
                        alert('成功')
                        this.refresh()
                    } else
                        alert('失敗')
                })
            },
            refresh(){
                axios.get('/User').then((resp) => {
                    const ret = resp.data
                    this.userList.splice(0, this.userList.length)
                    for (let user of ret) {
                        this.userList.push(user)
                    }
                })
            }
        },
        components: {}
    }
</script>
