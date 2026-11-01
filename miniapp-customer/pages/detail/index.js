/**
 * 卡详情：展示卡信息并进入下单
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getProduct } from '../../api/homeApi';
import { getCommentEligibility, pageProductComments } from '../../api/commentApi';

const COMMENT_PREVIEW_SIZE = 3;

Page({
  data: {
    loading: true,
    product: null,
    comments: [],
    commentTotal: 0,
    commentsLoading: false,
    canComment: false,
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadData();
  },

  onShow() {
    // 从写评论页返回时刷新评论与可评状态
    if (this.initialized) {
      this.loadComments();
    }
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const product = await getProduct(this.productId);
      this.setData({ product });
      this.initialized = true;
      this.loadComments();
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadComments() {
    this.setData({ commentsLoading: true });
    const [pageResult, eligibleResult] = await Promise.allSettled([
      pageProductComments(this.productId, 1, COMMENT_PREVIEW_SIZE),
      getCommentEligibility(this.productId),
    ]);
    const page = pageResult.status === 'fulfilled' ? pageResult.value : null;
    const canComment = eligibleResult.status === 'fulfilled' ? Boolean(eligibleResult.value) : false;
    this.setData({
      comments: page?.list ?? [],
      commentTotal: page?.total ?? 0,
      canComment,
      commentsLoading: false,
    });
  },

  goComments() {
    wx.navigateTo({ url: `/pages/comments/index?productId=${this.productId}` });
  },

  goCommentEdit() {
    if (!this.data.canComment) {
      wx.showToast({ title: '暂无可评论的订单', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/comment-edit/index?productId=${this.productId}` });
  },

  previewCommentImage(event) {
    const commentIndex = Number(event.currentTarget.dataset.commentIndex);
    const imageIndex = Number(event.currentTarget.dataset.imageIndex);
    const urls = this.data.comments[commentIndex]?.images ?? [];
    if (!urls.length) {
      return;
    }
    wx.previewImage({ current: urls[imageIndex], urls });
  },

  buy() {
    const { product } = this.data;
    if (product?.status !== 1) {
      wx.showToast({ title: '该卡已售罄', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/order-confirm/index?id=${product.id}` });
  },
});
