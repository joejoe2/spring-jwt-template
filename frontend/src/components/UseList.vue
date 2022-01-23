<template>
  <div class="row justify-content-center mt-3">
    <div class="card w-100 mb-4">
      <div class="card-body">
        <h2 class="card-title text-center">User List</h2>
        <ul class="list-group list-group-flush">
          <li class="list-group-item">
            <!-- Title bar -->
            <div class="row">
              <div class="col-2 col-sm-2 col-md-2 col-lg-1 fw-bold">
                <span>ID</span>
              </div>
              <div class="col-5 col-sm-4 col-md-3 col-lg-2 fw-bold">
                <span>Name</span>
              </div>
              <div class="col-sm-4 col-md-4 col-lg-2 d-none d-sm-block fw-bold">
                <span>Role</span>
              </div>
              <div class="col-lg-3 d-none d-lg-block fw-bold">
                <span>Email</span>
              </div>
              <div class="col-5 col-sm-4 col-md-3 col-lg-3 fw-bold">
                <span>Registered At</span>
              </div>
            </div>
          </li>
          <template v-for="user in userList.profiles" v-bind:key="user.id">
            <li class="list-group-item">
              <div class="row">
                <!-- User ID -->
                <div class="col-2 col-sm-2 col-md-2 col-lg-1">
                  <span>{{ user.id }}</span>
                </div>
                <!-- Username -->
                <div class="col-6 col-sm-4 col-md-3 col-lg-2">
                  <span>{{ user.username }}</span>
                </div>
                <!-- User role -->
                <div class="col-sm-4 col-md-4 col-lg-2 d-none d-sm-block">
                  <span>{{ user.role }} </span>
                </div>
                <!-- User email -->
                <div class="col-lg-3 d-none d-lg-block">
                  <span>{{ user.email }}</span>
                </div>
                <!-- User Registered At -->
                <div class="col-lg-3 d-none d-lg-block">
                  <span>{{ user.registeredAt }}</span>
                </div>
              </div>
            </li>
          </template>
        </ul>

        <nav aria-label="Page navigation UserList">
          <ul class="pagination justify-content-center">
            <li class="page-item">
              <a v-if="userList.currentPage>0" class="page-link" href="#" v-on:click="getUserList(userList.currentPage-1, userList.pageSize)">Previous</a>
            </li>
            <template v-for="pageNum in userList.totalPages" v-bind:key="pageNum">
                <li v-if="pageNum-1==userList.currentPage" class="page-item active">
                    <a class="page-link" href="#">{{ pageNum }}</a>
                </li>
                <li v-else class="page-item">
                    <a class="page-link" href="#" v-on:click="getUserList(pageNum-1, userList.pageSize)">{{ pageNum }}</a>
                </li>
            </template>
            <li class="page-item">
                <a v-if="userList.currentPage<userList.totalPages-1" class="page-link" href="#" v-on:click="getUserList(userList.currentPage+1, userList.pageSize)">Next</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
import store from "../store/index";

export default {
  name: "UserList",
  created() {
    this.getUserList(this.userList.currentPage, this.userList.pageSize);
  },
  data() {
    return {
      userList: {
        profiles: [],
        totalItems: 0,
        currentPage: 0,
        totalPages: 0,
        pageSize: 2,
      },
    };
  },
  methods: {
    getUserList(page, size) {
      store
        .dispatch("admin/getUserList", {
          page: page,
          size: size,
        })
        .then((userList) => {
          this.userList = userList;
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>

<style>
</style>