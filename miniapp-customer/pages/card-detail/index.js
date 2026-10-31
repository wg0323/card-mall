/**
 * 会员卡详情：渐变卡面 + 余次 + 使用记录时间轴
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getCard } from '../../api/cardApi';
import { decorateCard } from '../../utils/dict';

Page({
  data: {
    loading: true,
    card: null,
    records: [],
    usable: false,
    unusableReason: '',
  },

  onLoad(options) {
    this.cardId = options.id;
  },

  onShow() {
    // 核销后余次与记录都会变，每次回到本页重新拉
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const detail = await getCard(this.cardId);
      const card = decorateCard(detail.card);
      const expired = new Date(card.validEnd.replace(/-/g, '/')) <= new Date();
      const usedUp = card.cardType === 1 && card.remainingTimes <= 0;
      let unusableReason = '';
      if (card.status === 4) {
        unusableReason = '该卡已冻结，请联系门店';
      } else if (expired || card.status === 3) {
        unusableReason = '该卡已过期';
      } else if (usedUp || card.status === 2) {
        unusableReason = '次数已用完，可回首页续卡';
      }

      this.setData({
        card,
        records: (detail.records ?? []).map((item) => ({
          ...item,
          revoked: item.status === 2,
        })),
        usable: !unusableReason,
        unusableReason,
      });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ loading: false });
    }
  },

  showCode() {
    if (!this.data.usable) {
      wx.showToast({ title: this.data.unusableReason, icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/verify-code/index?id=${this.cardId}` });
  },
});
