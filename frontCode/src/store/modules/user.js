import { login, logout, getUserInfo } from '@/api/user';
import { getToken, setToken, removeToken } from '@/utils/auth';

const state = {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || {}
};

const getters = {
    isLoggedIn: state => !!state.token,
    userName: state => {
        console.log('User info in store:', state.userInfo);
        return state.userInfo.nickname || state.userInfo.username || state.userInfo.email || '用户';
    }
};

const mutations = {
    SET_TOKEN(state, token) {
        state.token = token;
        localStorage.setItem('token', token);
    },
    SET_USER_INFO(state, userInfo) {
        console.log('Setting user info:', userInfo);
        state.userInfo = userInfo;
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
    },
    CLEAR_USER_DATA(state) {
        state.token = '';
        state.userInfo = {};
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
    }
};

const actions = {
    // 用户登录
    login({ commit, dispatch }, { token, userInfo }) {
        commit('SET_TOKEN', token);
        if (userInfo) {
            commit('SET_USER_INFO', userInfo);
        } else {
            dispatch('fetchUserInfo');
        }
    },

    // 获取用户信息
    async fetchUserInfo({ commit, state }) {
        // 直接从localStorage获取token
        const token = localStorage.getItem('token');

        if (!token) {
            console.log('No token available for fetching user info');
            return;
        }

        // 检查token是否符合JWT格式
        if (!token.includes('.') || token.split('.').length !== 3) {
            console.error('Invalid JWT format:', token);
            // 清除无效token
            localStorage.removeItem('token');
            commit('CLEAR_USER_DATA');
            return;
        }

        try {
            const apiUrl = `${import.meta.env.VITE_API_BASE_URL || '/api'}/user/info`;
            console.log('Fetching user info from URL:', apiUrl);
            console.log('With token:', token);

            // 这里调用获取用户信息的API
            const response = await fetch(apiUrl, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log('User info response status:', response.status);

            if (response.ok) {
                const result = await response.json();
                console.log('User info response data:', result);

                // 根据API返回结构提取用户信息
                const userInfo = result.data || result.user || result.userInfo || result;
                commit('SET_USER_INFO', userInfo);
            } else {
                console.error('Failed to fetch user info, status:', response.status);

                // 如果是401或403，清除token
                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem('token');
                    commit('CLEAR_USER_DATA');
                }

                // 尝试读取错误响应
                try {
                    const errorData = await response.json();
                    console.error('Error response:', errorData);
                } catch (e) {
                    const errorText = await response.text();
                    console.error('Error response text:', errorText);
                }
            }
        } catch (error) {
            console.error('Failed to fetch user info:', error);
        }
    },

    // 用户登出
    logout({ commit }) {
        commit('CLEAR_USER_DATA');
    },

    // 前端登出
    fedLogout({ commit }) {
        return new Promise(resolve => {
            commit('CLEAR_USER_DATA');
            removeToken();
            resolve();
        });
    },

    setUserInfo({ commit }, userInfo) {
        commit('SET_USER_INFO', userInfo);
    }
};

export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions
}; 