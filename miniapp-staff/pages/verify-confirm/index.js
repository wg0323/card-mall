/**
 * 核销确认：预览只读，确认时才扣次；一次确认动作生成一个 requestId 做幂等
 * 作者：card-mall 创建日期：2026-10-31
 */
import { confirmVerify, previewVerify } from '../../api/staffApi';
import { requireLogin } from '../../utils/auth';

// 时间戳 + 两段随机数拼 32 位，同一台设备一次确认动作内不会重复
const newRequestId = () => {
  const random = () => Math.random().toString(16).slice(2, 10);
  return `${Date.now().toString(16)}${random()}${random()}`.slice(0, 32);
};

Page({
  data: {
    loading: true,
    card: null,
    loadError: '',
    times: 1,
    afterTimes: 0,
    remark: '',
    submitting: false,
  },

  onLoad(options) {
    if (!requireLogin()) {
      return;
    }
    this.token = options.token ?? '';
    this.loadPreview();
  },

  async loadPreview() {
    this.setData({ loading: true, loadError: '' });
    try {
      const preview = await previewVerify(this.token);
      this.setData({
        card: {
          ...preview,
          timesText: preview.cardType === 1 ? `${preview.remainingTimes}` : '不限次',
          timesUnit: preview.cardType === 1 ? '次' : '',
        },
        times: 1,
        afterTimes: preview.cardType === 1 ? Math.max(preview.remainingTimes - 1, 0) : 0,
      });
    } catch (error) {
      // 码失效、卡已用完/过期都在这里，原因展示在页面上
      this.setData({ card: null, loadError: error?.message ?? '核销码不可用' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onTimesChange(event) {
    const { card } = this.data;
    const times = Number(event.detail?.value ?? event.detail) || 1;
    this.setData({ times, afterTimes: Math.max((card?.remainingTimes ?? 0) - times, 0) });
  },

  onRemarkInput(event) {
    this.setData({ remark: event.detail.value });
  },

  async confirm() {
    const { card, times, remark, submitting } = this.data;
    if (submitting || !card) {
      return;
    }
    this.setData({ submitting: true });
    try {
      const result = await confirmVerify({
        token: this.token,
        requestId: newRequestId(),
        // 期限卡后端不扣次，但 times 仍须在 1..10 之间
        times: card.cardType === 1 ? times : 1,
        remark: remark.trim(),
      });
      const remaining = result.remainingTimes === null || result.remainingTimes === undefined ? '' : result.remainingTimes;
      const query = [
        `cardName=${encodeURIComponent(result.cardName ?? '')}`,
        `times=${result.verifyTimes ?? 0}`,
        `remaining=${remaining}`,
        `verifyTime=${encodeURIComponent(result.verifyTime ?? '')}`,
      ].join('&');
      wx.redirectTo({ url: `/pages/verify-result/index?${query}` });
    } catch (error) {
      // 请求层已提示；失败会 restore 核销码，重新预览拿到最新余次，避免按旧数字再扣
      this.loadPreview();
    } finally {
      this.setData({ submitting: false });
    }
  },

  goHome() {
    wx.reLaunch({ url: '/pages/home/index' });
  },
});
