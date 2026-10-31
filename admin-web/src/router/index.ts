import { createRouter, createWebHistory } from 'vue-router';
import { BRAND_NAME } from '@/config';
import { useUserStore } from '@/stores/user';

declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    public?: boolean;
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'product',
          name: 'product',
          component: () => import('@/views/product/index.vue'),
          meta: { title: '卡产品' },
        },
        {
          path: 'member',
          name: 'member',
          component: () => import('@/views/member/index.vue'),
          meta: { title: '会员' },
        },
        {
          path: 'order',
          name: 'order',
          component: () => import('@/views/order/index.vue'),
          meta: { title: '订单' },
        },
        {
          path: 'verify',
          name: 'verify',
          component: () => import('@/views/verify/index.vue'),
          meta: { title: '核销记录' },
        },
        {
          path: 'staff',
          name: 'staff',
          component: () => import('@/views/staff/index.vue'),
          meta: { title: '员工' },
        },
        {
          path: 'store',
          name: 'store',
          component: () => import('@/views/store/index.vue'),
          meta: { title: '门店设置' },
        },
      ],
    },
  ],
});

router.beforeEach((to) => {
  document.title = `${to.meta.title ?? ''} · ${BRAND_NAME} 商家后台`;
  const user = useUserStore();
  if (!to.meta.public && !user.token) {
    return { path: '/login' };
  }
  if (to.path === '/login' && user.token) {
    return { path: '/dashboard' };
  }
  return true;
});

export default router;
