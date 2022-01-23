<template>
  <div class="row justify-content-center mt-3">
    <div class="col-12 col-md-8 col-lg-8 justify-content-center">
      <div class="card w-100 mb-4">
        <div class="card-body">
          <h2 class="card-title text-center">Profile</h2>
          <ul class="list-group list-group-flush mb-2">
            <li class="list-group-item">
              <div class="row justify-content-center">
                <div class="col-6 align-self-center">
                  <span class="fs-5">ID</span>
                </div>
                <div class="col-6">
                  <span class="fs-5">{{ profile.id }}</span>
                </div>
              </div>
            </li>
            <li class="list-group-item">
              <div class="row justify-content-center">
                <div class="col-6 align-self-center">
                  <span class="fs-5">Username</span>
                </div>
                <div class="col-6">
                  <span class="fs-5">{{ profile.username }}</span>
                </div>
              </div>
            </li>
            <li class="list-group-item">
              <div class="row justify-content-center">
                <div class="col-6 align-self-center">
                  <span class="fs-5">Email</span>
                </div>
                <div class="col-6">
                  <span class="fs-5">{{ profile.email }}</span>
                </div>
              </div>
            </li>
            <li class="list-group-item">
              <div class="row justify-content-center">
                <div class="col-6 align-self-center">
                  <span class="fs-5">Role</span>
                </div>
                <div class="col-6">
                  <span class="fs-5">{{ profile.role }}</span>
                </div>
              </div>
            </li>
            <li class="list-group-item">
              <div class="row justify-content-center">
                <div class="col-6 align-self-center">
                  <span class="fs-5">Registered At</span>
                </div>
                <div class="col-6">
                  <span class="fs-5">{{ profile.registeredAt }}</span>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import store from "../store/index";

export default {
  name: "Profile",
  created() {
    this.getProfile();
  },
  data() {
    return {
      profile: { id: "", username: "", email: "", role: "", registeredAt: "" },
    };
  },
  methods: {
    getProfile() {
      store
        .dispatch("user/getProfile")
        .then((profile) => {
          this.profile = profile;
        }).catch(()=>{
          this.profile = "";
        })
    },
  },
};
</script>

<style scoped>
</style>