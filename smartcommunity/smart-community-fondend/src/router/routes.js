// 引入布局组件
const Layout = () => import('@/components/Layout/index.vue')

export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    hidden: true,
    meta: { title: '系统登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    // 关键：父路由也添加认证要求，保护 Layout
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '数据驾驶舱', icon: 'Odometer', requiresAuth: true }
      }
    ]
  },
  {
    path: '/gis',
    component: Layout,
    redirect: '/gis/index',
    meta: { title: 'GIS 一张图', icon: 'MapLocation', requiresAuth: true },
    children: [
      {
        path: 'index',
        name: 'GISMap',
        component: () => import('@/views/GIS/index.vue'),
        meta: { title: '空间分布', activeMenu: '/gis', requiresAuth: true }
      }
    ]
  },
  {
    path: '/grid',
    component: Layout,
    redirect: '/grid/map',
    meta: { title: '网格管理', icon: 'Grid', requiresAuth: true },
    children: [
      {
        path: 'map',
        name: 'GridDraw',
        component: () => import('@/views/GridManager/MapDraw.vue'),
        meta: { title: '网格绘制', icon: 'EditPen', requiresAuth: true }
      },
      {
        path: 'list',
        name: 'GridList',
        component: () => import('@/views/GridManager/index.vue'),
        meta: { title: '网格列表', icon: 'List', requiresAuth: true }
      }
    ]
  },
  {
    path: '/event',
    component: Layout,
    redirect: '/event/report', // 增加重定向，防止访问 /event 空白
    meta: { title: '事件处置', icon: 'Warning', requiresAuth: true },
    children: [
      {
        path: 'report',
        name: 'EventReport',
        component: () => import('@/views/EventManager/Report.vue'),
        meta: { title: '事件上报', requiresAuth: true }
      },
      {
        path: 'audit',
        name: 'EventAudit',
        component: () => import('@/views/EventManager/Audit.vue'),
        meta: { title: '事件审核', requiresAuth: true }
      }
    ]
  },
  // 补全系统管理路由
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', requiresAuth: true },
    children: [
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/System/User.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAuth: true }
      },
      {
        path: 'dept',
        name: 'DeptManage',
        component: () => import('@/views/System/Dept.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Error/404.vue'),
    hidden: true
  }
]
