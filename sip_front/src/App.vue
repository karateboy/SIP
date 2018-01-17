<template>
    <div id="app">
        <nav class="navbar-default navbar-static-side" role="navigation">
            <menu-view></menu-view>
        </nav>

        <div id="page-wrapper" class="gray-bg">
            <div class="row border-bottom">
                <nav class="navbar navbar-static-top white-bg" role="navigation" style="margin-bottom: 0">
                    <div class="navbar-header">
                        <a class="navbar-minimalize minimalize-styl-2 btn btn-primary " href="#"><i
                                class="fa fa-bars"></i> </a>
                        <form role="search" class="navbar-form-custom" method="post" action="#">
                            <div class="form-group">
                                <input type="text" placeholder="Gder+" class="form-control" name="top-search"
                                       id="top-search">
                            </div>
                        </form>
                    </div>
                    <ul class="nav navbar-top-links navbar-right">
                        <li>
                            <a @click="logout">
                                <i class="fa fa-sign-out"></i> 登出
                            </a>
                        </li>
                    </ul>

                </nav>
            </div>
            <div class="wrapper wrapper-content animated fadeInRight">
                <div class="row">
                    <div class="col-sm-12">
                        <router-view></router-view>
                    </div>
                </div>
            </div>
            <div class="footer">
                <div class="pull-right">
                    ...
                </div>
                <div>
                    <strong>Copyright</strong> Gder+ &copy; 2018
                </div>
            </div>

        </div>
    </div>

</template>

<script>
    import menu from "./components/menu.vue"
    import axios from 'axios'

    export default {
        name: 'app',
        data () {
            return {
                msg: 'Welcome to Your Vue.js App'
            }
        },
        methods: {
            logout(){
                axios.get("/logout").then((resp) => {
                    const ret = resp.data
                    if(!ret.ok)
                        console.log(ret.msg)

                    this.$store.commit('updateAuthenticated', {authenticated: false, user: {}});
                    this.$router.push({name: 'Login'})
                    alert("登出")
                }).catch((err) => {
                    alert(err)
                })
            }
        },
        components: {
            menuView: menu
        }
    }
</script>

<style>

</style>
