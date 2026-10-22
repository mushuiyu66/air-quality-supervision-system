//导入vue-router
import { createRouter, createWebHistory } from 'vue-router'
//导入组件
import login from '@/views/Login.vue'
import AdminLayout from '@/views/admin/AdminLayout.vue'
import FeedbackManage from '@/views/admin/FeedbackManage.vue'
import MemberList from '@/views/admin/MemberList.vue'
import SupervisorManage from '@/views/admin/SupervisorManage.vue'
import AqiManage from '@/views/admin/AqiManage.vue'
import RegionManage from '@/views/admin/RegionManage.vue'
import SupervisorFeedBack from '@/views/supervisorFeedBack.vue'
import GridMemberLayout from '@/views/gridMember/GridMemberLayout.vue'
import { ElMessage } from 'element-plus'

//定义路由关系
const routes = [
    {
        path: '/',
        component: login,
        meta: { title: '登录' }   
    },
    {
        path: '/supervisorfeedback',
        component: SupervisorFeedBack,
        meta: { title: '市民反馈', requiresAuth: true, allowedRoles: ['supervisor'] }
    },
    {
        path: '/admin',
        component: AdminLayout,
        meta: { title: '管理员后台', requiresAuth: true, allowedRoles: ['admin'] },
        children: [
            {
                path: 'home', // 首页
                name: 'AdminHome',
                component: () => import('@/views/admin/Dashboard.vue'),
                meta: { title: '首页仪表盘' }
            },
            {
                path: 'feedbacks', // 工单管理
                name: 'FeedbackManage',
                component: FeedbackManage,
                meta: { title: '工单管理' }
            },
            {
                path: 'members', // 网格员管理
                name: 'MemberList',
                component: MemberList,
                meta: { title: '网格员管理' }
            },
            {
                path: 'supervisors', // 市民监督员管理
                name: 'SupervisorManage',
                component: SupervisorManage,
                meta: { title: '市民监督员管理' }
            },
            {
                path: 'aqi-standards', // AQI标准管理
                name: 'AqiManage',
                component: AqiManage,
                meta: { title: 'AQI标准管理' }
            },
            {
                path: 'regions', // 省市区域管理
                name: 'RegionManage',
                component: RegionManage,
                meta: { title: '省市区域管理' }
            },
            {
                path: 'statistics', // 数据统计
                name: 'Statistics',
                component: () => import('@/views/admin/Statistics.vue'),
                meta: { title: '数据统计' }
            }
        ]
    },
    {
        path: '/gridmember',
        component: GridMemberLayout,
        meta: { title: '网格员操作界面', requiresAuth: true, allowedRoles: ['grid'] },
        children: [
            {
                path: 'home', // 首页仪表盘
                name: 'GridMemberHome',
                component: () => import('@/views/gridMember/Dashboard.vue'),
                meta: { title: '首页仪表盘' }
            },
            {
                path: 'tasks', // 工单处理
                name: 'GridMemberTasks',
                component: () => import('@/views/gridMember/TaskList.vue'),
                meta: { title: '工单处理' }
            }
        ]
    }
]

// 定义不需要登录的路由路径
const publicPaths = ['/']

// 角色路由映射
const roleRoutes = {
    admin: '/admin/home',
    grid: '/gridmember/home',
    supervisor: '/supervisorfeedback'
}

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 获取角色（不使用 token）
    const role = localStorage.getItem('role')
    
    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title + ' - 网格管理系统'
    }
    
    // 判断是否为根路径（登录页）
    if (to.path === '/') {
        // 如果是未登录状态，正常显示登录页
        if (!role) {
            next()
        } else {
            // 已登录状态访问根路径，重定向到对应角色的首页
            if (role && roleRoutes[role]) {
                next(roleRoutes[role])
            } else {
                next('/')
            }
        }
        return
    }
    
    // 需要登录的页面
    if (to.meta.requiresAuth) {
        if (!role) {
            // 未登录，提示并跳转到登录页
            ElMessage.warning('请先登录')
            next('/')
        } else {
            // 已登录，校验角色权限
            const allowedRoles = to.meta.allowedRoles || ['admin', 'grid', 'supervisor']
            if (!allowedRoles.includes(role)) {
                // 角色不匹配，重定向到对应角色的首页
                ElMessage.warning('您无权访问该页面')
                if (role && roleRoutes[role]) {
                    next(roleRoutes[role])
                } else {
                    next('/')
                }
            } else {
                // 角色匹配，允许访问
                next()
            }
        }
    } else {
        // 不需要登录的页面，直接访问
        next()
    }
})

export default router