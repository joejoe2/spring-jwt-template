import api from './api';
import axios from 'axios';
import authHeader from './auth-header';

function parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
}

class AuthService {
    login(username, password) {
        return api
            .post('/auth/login', {
                username: username,
                password: password
            })
            .then(response => {
                let info = parseJwt(response.data.access_token);
                let user = {
                    id: info.id,
                    username: info.username,
                    role: info.role,
                    isActive: info.isActive,
                    access_token: response.data.access_token,
                    refresh_token: response.data.refresh_token,
                }
                localStorage.setItem('user', JSON.stringify(user));
                return user;
            });
    }

    logout() {
        return api.post('/auth/logout', {}, {
            headers: authHeader()
        }).then(response => {
            if (response.status == 200) {
                localStorage.removeItem('user');
            }
        });
    }

    register(username, password, email) {
        return api.post('/auth/register', {
            username: username,
            email: email,
            password: password
        });
    }

    refresh(user) {
        return axios
            .post(api.defaults.baseURL + '/auth/refresh', {
                token: user.refresh_token,
            }, api.defaults.headers)
            .then(response => {
                let info = parseJwt(response.data.access_token);
                let user = {
                    id: info.id,
                    username: info.username,
                    role: info.role,
                    isActive: info.isActive,
                    access_token: response.data.access_token,
                    refresh_token: response.data.refresh_token,
                }
                localStorage.setItem('user', JSON.stringify(user));
                return user;
            }).catch((error) => {
                localStorage.removeItem('user');
                return Promise.reject(error);
            });
    }
}

export default new AuthService();