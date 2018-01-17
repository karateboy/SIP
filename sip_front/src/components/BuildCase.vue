<template>
    <div class="ibox flot-e-margins">
        <div class="ibox-content">
            <ul class="nav nav-tabs">                
                <router-link  v-show="user.groupId == 'Admin'" to='/BuildCase/New' tag='li' role="presentation" active-class='active'><a>新增起造人</a>
                </router-link>
                <router-link to='/BuildCase/Query' tag='li' role="presentation" active-class='active'><a>查詢起造人</a>
                </router-link>
                <router-link to='/BuildCase/Import' tag='li' role="presentation" active-class='active'><a>匯入起造人</a>
                </router-link>
            </ul>
            <router-view></router-view>
        </div>
    </div>
</template>
<style scoped>

</style>
<script>
import { mapGetters } from "vuex";
export default {
  data() {
    return {};
  },
  computed: {
    ...mapGetters(["user"])
  }
};
</script>
