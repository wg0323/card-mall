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
      const hasCount = typeof count === 'number';
      if (!hasToken()) {
        // 登录态失效时在途请求的结果也要作废，避免角标又被写回来
        this.badgeSeq = (this.badgeSeq ?? 0) + 1;
        this.applyBadge(0);
        return;
      }
      const seq = (this.badgeSeq ?? 0) + 1;
      this.badgeSeq = seq;
      if (!hasCount) {
        // 每个 tab 页各有一个实例，先用全局缓存顶上，避免切页时闪回旧数字
        this.applyBadge(getApp().globalData.unreadCount);
      }
      try {
        const unread = hasCount ? count : await getUnreadCount();
        if (seq !== this.badgeSeq) {
          // 已有更新的请求发出，丢弃旧结果
          return;
        }
        this.applyBadge(unread);
      } catch (error) {
        // 请求层已提示
      }
    },

    // 角标与全局缓存一起更新，供其它 tab 页 onShow 时先渲染
    applyBadge(count) {
      const unread = Number.isFinite(Number(count)) ? Number(count) : 0;
      getApp().globalData.unreadCount = unread;
      this.setData({ messageBadge: { count: unread } });
    },
  },
});
