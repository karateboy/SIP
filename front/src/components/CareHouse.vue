<template>
    <div class="ibox flot-e-margins">
        <div class="ibox-content">
            <ul class="nav nav-tabs">
                <router-link to='/CareHouse/Query' tag='li' role="presentation" active-class='active'><a>查詢安養單位</a>
                </router-link>
            </ul>
            <router-view></router-view>
        </div>
    </div>
</template>
<style scoped>
</style>
<script>
    export default{
        data(){
            return {
                msg: '訂單主畫面'
            }
        },
        components: {}
    }
</script>
