import { createStore } from "vuex";
import { auth } from "./auth.module";
import { user } from "./user.module";
import { admin } from "./admin.module";

const store = createStore({
  modules: {
    auth,
    user,
    admin,
  },
});

export default store;