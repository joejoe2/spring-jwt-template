import adminService from "../services/admin.service";

export const admin = {
    namespaced: true,
    state: {},
    actions: {
        getUserList(context, pageParams) {
            return adminService.getUserList(pageParams.page, pageParams.size).then(userList => {
                return Promise.resolve(userList);
            }).catch(error => {
                error.message = error.response.data.info || "Unknown error, please try again later !"
                return Promise.reject(error);
            });
        }
    },
    mutations: {}
}