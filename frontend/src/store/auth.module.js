import AuthService from '../services/auth.service';

const user = JSON.parse(localStorage.getItem('user'));
const initialState = user
    ? { status: { loggedIn: true }, user }
    : { status: { loggedIn: false }, user: null };

export const auth = {
    namespaced: true,
    state: initialState,
    actions: {
        login(context, payload) {
            return AuthService.login(payload.username, payload.password).then(
                user => {
                    context.commit('loginSuccess', user);
                    return Promise.resolve(user);
                }).catch(error => {
                    context.commit('loginFailure');
                    if (error.response && error.response.status == 400) {
                        error.message = "Incorrect username or password !"
                    } else {
                        error.message = "Unknown error, please try again later !"
                    }
                    return Promise.reject(error);
                });
        },
        logout(context) {
            return AuthService.logout().then(data => {
                context.commit('logoutSuccess');
                return Promise.resolve(data);
            }).catch(error => {
                if (error.response && error.response.status == 400) {
                    error.message = "Invalid token !"
                } else {
                    error.message = "Unknown error, please try again later !"
                }
                return Promise.reject(error);
            });
        },
        register(context, payload) {
            return AuthService.register(payload.username, payload.password, payload.email).then(
                response => {
                    context.commit('registerSuccess');
                    return Promise.resolve(response.data);
                },
                error => {
                    context.commit('registerFailure');
                    return Promise.reject(error);
                }
            );
        },
        refresh(context) {
            return AuthService.refresh(context.state.user).then(
                user => {
                    context.commit('refreshSuccess', user);
                    return Promise.resolve(user);
                }).catch(error => {
                    context.commit('refreshFailure');
                    if (error.response && error.response.status == 400) {
                        error.message = "Invalid token !"
                    } else {
                        error.message = "Unknown error, please try again later !"
                    }
                    return Promise.reject(error);
                });
        },
    },
    mutations: {
        loginSuccess(state, user) {
            state.status.loggedIn = true;
            state.user = user;
        },
        loginFailure(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
        logoutSuccess(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
        registerSuccess(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
        registerFailure(state) {
            state.status.loggedIn = false;
            state.user = null;
        },
        refreshSuccess(state, user){
            state.status.loggedIn = true;
            state.user = user;
        },
        refreshFailure(state){
            state.status.loggedIn = false;
            state.user = null;
        },
    }
};