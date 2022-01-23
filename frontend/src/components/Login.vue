<template>
  <div class="row justify-content-center mt-3">
    <div class="col-12 col-md-8 col-lg-4 justify-content-center">
      <h2>Login</h2>
      <div class="card w-100 mb-4">
        <div class="card-body">
          <img
            id="profile-img"
            src="//ssl.gstatic.com/accounts/ui/avatar_2x.png"
            class="profile-img-card"
          />

          <div class="mb-2">
            <label for="username">Username</label>
            <input
              v-model="username"
              name="username"
              type="text"
              class="form-control"
            />
          </div>
          <div class="mb-2">
            <label for="password">Password</label>
            <input
              v-model="password"
              name="password"
              type="password"
              class="form-control"
            />
          </div>
          <div class="mb-2">
            <button class="btn btn-success" v-on:click="login">
              <span>Login</span>
            </button>
            or
            <a href="#" v-on:click="toRegister">register now !</a>
          </div>
          <div class="mb-2">
            <div v-if="errorMsg" class="alert alert-danger" role="alert">
              {{ errorMsg }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import store from "../store/index";

export default {
  name: "Login",
  data() {
    return {
      username: "",
      password: "",
      errorMsg: "",
    };
  },
  computed: {},
  methods: {
    login() {
      store
        .dispatch("auth/login", {
          username: this.username,
          password: this.password,
        })
        .then(() => {
          this.$router.back();
        })
        .catch((error) => {
          if (error.message) {
            this.errorMsg = error.message;
          } else {
            console.log(error);
          }
        });
    },
    toRegister(){
      this.$router.push({name: 'Register'});
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>