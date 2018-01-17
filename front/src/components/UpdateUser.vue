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
                               @click="selectedUser=user">
                            <input type="radio">{{ user.name }} </label>
                    </div>
                </div>
            </div>
        </div>
        <div v-if='selectedUser'>
            <user :user='selectedUser' :isNew='false'></user>
        </div>
    </div>
</template>
<style>
    body{
        background-color:#ff0000;
    }

</style>
<script>
    import User from './User.vue'
    import axios from 'axios'

    export default{
        data(){
            this.refresh()
            return {
                userList: [],
                selectedUser: null
            }
        },
        methods: {

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
        components: {
            User
        }
    }
</script>
