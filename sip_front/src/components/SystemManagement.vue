<template>
    <div class="ibox-content">
        <ul class="nav nav-tabs">
            <router-link tag="li"
                         :to="{name:'AddUser'}" active-class="active">
                <a><i class="fa fa-th-large"></i>
                    <span class="nav-label">新增使用者</span> </a></router-link>

            <router-link tag="li"
                         :to="{name:'DelUser'}" active-class="active">
                <a><i class="fa fa-th-large"></i>
                    <span class="nav-label">刪除使用者</span> </a></router-link>

            <router-link tag="li"
                         :to="{name:'UpdateUser'}" active-class="active">
                <a><i class="fa fa-th-large"></i>
                    <span class="nav-label">更新使用者</span> </a></router-link>
        </ul>
        <router-view></router-view>
    </div>
</template>
<style>
    body{
        background-color:#ff0000;
    }

</style>
<script>
    export default{
        data(){
            return {
                msg: 'hello vue'
            }
        },
        components: {}
    }
</script>
