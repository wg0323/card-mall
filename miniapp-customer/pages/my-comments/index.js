/**
 * 我的评论：查看店长回复、删除自己的评论
 * 作者：card-mall 创建日期：2026-11-01
 */
import { deleteComment, pageMyComments } from '../../api/commentApi';
import { requireLogin } from '../../utils/auth';
import { decorateMyComment } from '../../utils/dict';

const PAGE_SIZE = 10;

Page({
  data: {
    list: [],
    total: 0,
    loading: true,
  },

  onShow() {
    if (!requireLogin()) {
      return;
    }
    this.pageNum = 1;
    this.hasMore = true;
    this.loading = false;
    this.load(true);
  },

  onReachBottom() {
    this.load(false);
  },

  async load(reset) {
    if (this.loading) {
      return;
    }
    if (!reset && !this.hasMore) {
      return;
    }
    this.loading = true;
    if (reset) {
      this.setData({ loading: true });
    }
    try {
      const pageNum = reset ? 1 : this.pageNum;
      const page = await pageMyComments(pageNum, PAGE_SIZE);
      const list = (page?.list ?? []).map(decorateMyComment);
      const merged = reset ? list : [...this.data.list, ...list];
      this.setData({
        list: merged,
        total: page?.total ?? 0,
        loading: false,
      });
      this.pageNum = pageNum + 1;
      this.hasMore = merged.length < (page?.total ?? 0);
    } catch (error) {
      this.setData({ loading: false });
      this.hasMore = false;
    } finally {
      this.loading = false;
    }
  },

  previewImage(event) {
    const commentIndex = Number(event.currentTarget.dataset.commentIndex);
    const imageIndex = Number(event.currentTarget.dataset.imageIndex);
    const urls = this.data.list[commentIndex]?.images ?? [];
    if (!urls.length) {
      return;
    }
    wx.previewImage({ current: urls[imageIndex], urls });
  },

  async remove(event) {
    const id = Number(event.currentTarget.dataset.id);
    let confirmed = false;
    try {
      const result = await new Promise((resolve, reject) => {
        wx.showModal({
          title: '删除评论',
          content: '删除后不可恢复，且该订单不能再次评论',
          confirmText: '删除',
          cancelText: '取消',
          success: resolve,
          fail: reject,
        });
      });
      confirmed = result.confirm;
    } catch (error) {
      return;
    }
    if (!confirmed) {
      return;
    }
    try {
      await deleteComment(id);
      wx.showToast({ title: '已删除', icon: 'success' });
      this.pageNum = 1;
      this.hasMore = true;
      this.loading = false;
      this.load(true);
    } catch (error) {
      // 请求层已提示
    }
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },
});
