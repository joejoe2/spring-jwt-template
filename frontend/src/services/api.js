import axios from "axios";
import store from "../store/index"
import router from "../router";
import authHeader from "./auth-header"

const instance = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: { 'Content-Type': 'application/json' },
    timeout: 10000,
});

instance.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

instance.interceptors.response.use(
    function (response) {
        return response;
    },
    async function (error) {
        if (error.response && error.response.status == 401) {
            if (store.state.auth.status.loggedIn) {
                let success = false;
                await store.dispatch('auth/refresh').then(() => {
                    success = true;
                    error.config.headers = authHeader();
                }).catch(() => {
                    success = false;
                    error.message = "refresh token expired, please login again !";
                });
                if (success)
                    return instance(error.config);
                else
                    router.push({ name: "Login" });
            } else {
                router.push({ name: "Login" });
            }
        }else{
            return Promise.reject(error);
        }
    }
);

export default instance;