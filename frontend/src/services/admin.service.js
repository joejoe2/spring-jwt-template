import api from './api';
import authHeader from './auth-header';

class AdminService {
    getUserList(page, size) {
        return api.post('/admin/getUserList', { page: page, size: size }, { headers: authHeader() })
            .then(response => {
                return response.data;
            });
    }
}

export default new AdminService();