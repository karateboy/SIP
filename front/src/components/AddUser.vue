<template>
    <div>
        <br>
        <user :user='{}' :isNew='true'></user>
    </div>
</template>
<style>
    body{
        background-color:#ff0000;
    }

</style>
<script>
    import User from './User.vue'
    export default{
        data(){
            return {
                msg: 'hello vue'
            }
        },
        components: {
            User
        }
    }
</script>
