<template>
  <div class="row justify-content-center mt-3">
    <div class="col-12 col-md-8 col-lg-4 justify-content-center">
      <h2>Register</h2>
      <div class="card w-100 mb-4">
        <div class="card-body">
          <div class="mb-2">
            <div v-if="errorMsg" class="alert alert-danger" role="alert">
              {{ errorMsg }}
            </div>
          </div>
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
            <label for="email">Email</label>
            <input
              v-model="email"
              name="email"
              type="email"
              class="form-control"
            />
          </div>
          <div class="mb-2">
            <button class="btn btn-success" v-on:click="register">
              <span>Register</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import store from "../store/index";

export default {
  name: "Register",
  created() {
    if (store.state.auth.status.loggedIn) {
      this.$router.replace({ name: "Home" });
    }
  },
  data() {
    return {
      username: "",
      password: "",
      email: "",
      errorMsg: "",
    };
  },
  methods: {
    register() {
      store
        .dispatch("auth/register", {
          username: this.username,
          password: this.password,
          email: this.email
        })
        .then(() => {
          this.$router.push({name: "Login"});
        })
        .catch((error) => {
          if (error.message) {
            this.errorMsg = error.response.data.info || error.message;
          } else {
            console.log(error);
          }
        });
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>