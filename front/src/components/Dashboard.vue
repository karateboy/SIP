<template>
    <div>
        <h1>Gder+管理系統</h1>
    </div>
</template>
<style>

</style>
<script>
    import {mapActions, mapGetters} from 'vuex';

    export default{
        data(){
            return {
                msg: '儀錶板...TBD'
            }
        },
        computed: {
            ...mapGetters(['isAuthenticated', 'user']),
        },
        methods: {
                ...mapActions(['authenticate']),
            test(evt){
                    this.$store.dispatch('testAuthenticated');
            }
        },
        components: {}
    }
</script>
