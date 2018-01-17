<template>
    <div class="ibox flot-e-margins">
        <div class="ibox-content">
            <ul class="nav nav-tabs">
                <router-link to='/Intern/Builder' tag='li' role="presentation" active-class='active'><a>起造人電話</a>
                </router-link>
                <router-link to='/Intern/BuildCase' tag='li' role="presentation" active-class='active'><a>建案資訊</a>
                </router-link>
                <router-link to='/Intern/Report' tag='li' role="presentation" active-class='active'><a>工作報告</a>
                </router-link>
            </ul>
            <br>
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
