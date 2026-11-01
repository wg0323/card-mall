/**
 * 用户评论：某个卡产品的全部评论
 * 作者：card-mall 创建日期：2026-11-01
 */
import { getCommentEligibility, pageProductComments } from '../../api/commentApi';
import { requireLogin } from '../../utils/auth';

const PAGE_SIZE = 10;

Page({
  data: {
    list: [],
    total: 0,
    loading: true,
    canComment: false,
  },

  onLoad(options) {
    if (!requireLogin()) {
      return;
    }
    this.productId = options.productId;
    if (!this.productId) {
      wx.showToast({ title: '缺少卡产品参数', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.ready = true;
    this.pageNum = 1;
    this.hasMore = true;
    this.loading = false;
    this.loadEligibility();
    this.load(true);
  },

  onShow() {
    if (!this.ready) {
      return;
    }
    // 从写评论页返回时刷新评论与可评状态
    if (this.initialized) {
      this.pageNum = 1;
      this.hasMore = true;
      this.loading = false;
      this.load(true);
      this.loadEligibility();
    }
    this.initialized = true;
  },

  onReachBottom() {
    this.load(false);
  },

  async loadEligibility() {
    try {
      const canComment = await getCommentEligibility(this.productId);
      this.setData({ canComment: Boolean(canComment) });
    } catch (error) {
      this.setData({ canComment: false });
    }
  },

  async load(reset) {
    if (!this.ready || this.loading) {
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
      const page = await pageProductComments(this.productId, pageNum, PAGE_SIZE);
      const list = page?.list ?? [];
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

  goWrite() {
    if (!this.data.canComment) {
      wx.showToast({ title: '暂无可评论的订单', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/comment-edit/index?productId=${this.productId}` });
  },
});
