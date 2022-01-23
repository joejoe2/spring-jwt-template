import { createWebHistory, createRouter } from "vue-router";
import store from "./store";
import Profile from "./components/Profile.vue";
import Login from "./components/Login.vue";
import Home from "./components/Home.vue";
import Register from './components/Register.vue';
import Admin from './components/Admin.vue';

const routes = [
  {
    path: "/profile",
    name: "Profile",
    component: Profile,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/home",
    name: "Home",
    component: Home,
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/admin",
    name: "Admin",
    component: Admin,
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.afterEach((to) => {
  if ((to.name != "Login" && to.name != "Register") && !store.state.auth.status.loggedIn) {
    router.push({ name: "Login" });
  } else if ((to.name == "Login" || to.name == "Register") && store.state.auth.status.loggedIn) {
    router.replace({ name: "Home" });
  }

  if (to.name == "Admin" && store.state.auth.user.role != "ADMIN") {
    router.replace({ name: "Home" });
  }
});

export default router;