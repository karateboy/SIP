<template>
    <div class="ibox flot-e-margins">
        <div class="ibox-content">
            <ul class="nav nav-tabs">
                <router-link to='/OilUser/Query' tag='li' role="presentation" active-class='active'><a>加油站/油槽/鍋爐</a>
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
