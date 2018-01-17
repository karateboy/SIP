<template>
    <div class="ibox-content">
        <ul class="nav nav-tabs">
            <router-link tag="li"
                         :to="{name:'AddVehicle'}" active-class="active">
                <a><i class="fa fa-plus" aria-hidden="true"></i>
                    <span class="nav-label">新增車輛</span> </a></router-link>

            <router-link tag="li"
                         :to="{name:'DelVehicle'}" active-class="active">
                <a><i class="fa fa-trash" aria-hidden="true"></i>
                    <span class="nav-label">刪除車輛</span> </a></router-link>

            <router-link tag="li"
                         :to="{name:'UpdateVehicle'}" active-class="active">
                <a><i class="fa fa-pencil" aria-hidden="true"></i>
                    <span class="nav-label">更新車輛資料</span> </a></router-link>
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
            return{
                msg:'hello vue'
            }
        },
        components:{
        }
    }
</script>
