import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/match'
    },
    {
        path: '/match',
        name: 'Match',
        component: () => import('@/views/match/index.vue')
    },
    {
        path: '/match/history',
        name: 'MatchHistory',
        component: () => import('@/views/match/history.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router 