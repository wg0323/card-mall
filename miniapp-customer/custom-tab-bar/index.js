/**
 * 自定义 tabBar：用 TDesign 图标组件，避免往仓库塞 81x81 的 PNG
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getUnreadCount } from '../api/messageApi';
import { hasToken } from '../utils/auth';

Component({
  data: {
    value: '/pages/index/index',
    // t-badge 的 count 为 0 时不渲染，所以这里不需要额外判断
    messageBadge: { count: 0 },
  },
  methods: {
    onChange(event) {
      const { value } = event.detail;
      if (value === this.data.value) {
        return;
      }
      this.setData({ value });
      wx.switchTab({ url: value });
    },

    // 页面 onShow 时调用；已经拿到数字就传进来，省一次请求
    async refreshBadge(count) {
      if (!hasToken()) {
        this.setData({ messageBadge: { count: 0 } });
        return;
      }
      try {
        const unread = typeof count === 'number' ? count : await getUnreadCount();
        this.setData({ messageBadge: { count: Number(unread ?? 0) } });
      } catch (error) {
        // 请求层已提示
      }
    },
  },
});
