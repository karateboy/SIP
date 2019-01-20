<style lang="less">
@import "./login.less";
</style>

<template>
    <div class="login" @keydown.enter="handleSubmit">
        <div class="login-con">
            <Card :bordered="false">
                <p slot="title">
                    <Icon type="log-in"></Icon>
                    歡迎登入
                </p>
                <div class="form-con">
                    <Form ref="loginForm" :model="form" :rules="rules">
                        <FormItem prop="userName">
                            <Input v-model="form.userName" placeholder="請輸入帳號">
                                <span slot="prepend">
                                    <Icon :size="16" type="person"></Icon>
                                </span>
                            </Input>
                        </FormItem>
                        <FormItem prop="password">
                            <Input type="password" v-model="form.password" placeholder="請輸入密碼">
                                <span slot="prepend">
                                    <Icon :size="14" type="locked"></Icon>
                                </span>
                            </Input>
                        </FormItem>
                        <FormItem>
                            <Button @click="handleSubmit" type="primary" long>登入</Button>
                        </FormItem>
                    </Form>
                    <p class="login-tip">公用帳號sales@wecc.com.tw</p>
                </div>
            </Card>
        </div>
    </div>
</template>

<script>
import Cookies from "js-cookie";
import axios from "axios";

export default {
  data() {
    return {
      form: {
        userName: "sales@wecc.com.tw",
        password: ""
      },
      rules: {
        userName: [
          { required: true, message: "帳號不能是空的", trigger: "blur" }
        ],
        password: [
          { required: true, message: "密碼不能是空的", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    handleSubmit() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          let credential = {
            account: this.form.userName,
            password: this.form.password
          };
          axios
            .post("/authenticate", credential)
            .then(resp => {
              const ret = resp.data;
              const status = resp.status;
              if (status === 200) {
                if (ret.ok === true) {
                  Cookies.set("user", ret.user.name);
                  Cookies.set("userObj", ret.user);
                  console.log(ret.user);
                  this.$store.commit(
                    "setAvator",
                    "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3448484253,3685836170&fm=27&gp=0.jpg"
                  );
                  if (this.form.userName === "iview_admin") {
                    Cookies.set("access", 0);
                  } else {
                    Cookies.set("access", 1);
                  }
                  this.$router.push({
                    name: "home_index"
                  });
                } else {
                  alert(ret.msg);
                }
              }
            })
            .catch(err => alert(err));
        }
      });
    }
  }
};
</script>

<style>
</style>
