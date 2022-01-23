import UserService from '../services/user.service';

export const user = {
    namespaced: true,
    state: {
        profile: {
            id: "",
            username: "",
            email: "",
            role: "",
            registeredAt: ""
        },
    },
    actions: {
        getProfile(context) {
            return UserService.getProfile().then(profile => {
                context.commit('setProfile', profile);
                return Promise.resolve(profile);
            }).catch(error => {
                error.message = "Unknown error, please try again later !"
                return Promise.reject(error);
            });
        }
    },
    mutations: {
        setProfile(state, profile){
            state.profile = profile;
        },
    }
}