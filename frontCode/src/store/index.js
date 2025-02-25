import { createStore } from 'vuex';
import user from './modules/user';

export default createStore({
    modules: {
        user
    },
    plugins: [
        store => {
            store.subscribe((mutation, state) => {
                console.log('Mutation:', mutation.type, mutation.payload);
                if (mutation.type === 'user/SET_TOKEN') {
                    console.log('Token after mutation:', state.user.token);
                }
            });
        }
    ]
}); 