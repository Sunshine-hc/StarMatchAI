import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import store from '@/store'

const routes = [
    {
        path: '/',
        name: 'Match',
        component: () => import('../views/match/index.vue'),
        meta: { title: 'AI星座匹配' }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/auth/Login.vue'),
        meta: { title: 'AI星座匹配 - 登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/auth/Register.vue'),
        meta: { title: 'AI星座匹配 - 注册' }
    },
    {
        path: '/history',
        name: 'History',
        component: () => import('../views/history/index.vue'),
        meta: { title: 'AI星座匹配 - 历史记录', requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: 'AI星座匹配 - 个人中心', requiresAuth: true }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('../layout/index.vue'),
        redirect: '/home',
        children: [
            {
                path: 'home',
                name: 'Home',
                component: () => import('../views/home/index.vue'),
                meta: { title: '首页', requiresAuth: true }
            },
            // {
            //     path: 'match/history',
            //     name: 'MatchHistory',
            //     component: () => import('../views/match/history.vue'),
            //     meta: { title: '匹配历史', requiresAuth: true }
            // },
            // {
            //     path: 'match/create',
            //     name: 'CreateMatch',
            //     component: () => import('../views/match/create.vue'),
            //     meta: { title: '创建匹配', requiresAuth: true }
            // },
            // {
            //     path: 'match/detail/:id',
            //     name: 'MatchDetail',
            //     component: () => import('../views/match/detail.vue'),
            //     meta: { title: '匹配详情', requiresAuth: true }
            // }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/error/404.vue'),
        meta: { title: '404' }
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title || 'AI星座匹配'

    // 检查是否需要登录
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const token = localStorage.getItem('token')
        if (!token) {
            next({ path: '/login', query: { redirect: to.fullPath } })
        } else {
            next()
        }
    } else {
        next()
    }
})

export default router 