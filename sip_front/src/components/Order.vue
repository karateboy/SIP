<template>
    <div class="ibox flot-e-margins">
        <div class="ibox-content">
            <ul class="nav nav-tabs">
                <router-link to='/Order/Unhandled' tag='li' role="presentation" active-class='active'><a>未聯絡通報</a>
                </router-link>
                <router-link to='/Order/Query' tag='li' role="presentation" active-class='active'><a>查詢訂單</a>
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
