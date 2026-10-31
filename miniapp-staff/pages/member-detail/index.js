/**
 * 会员详情：从会员查询点进来，看会员资料、持卡与订单
 * 作者：card-mall 创建日期：2026-10-31
 */
import { getMemberDetailByPhone } from '../../api/staffApi';
import { requireLogin } from '../../utils/auth';
import { decorateCard, decorateOrder } from '../../utils/dict';

// 注册时间只展示到日：后端给的是 yyyy-MM-dd HH:mm:ss，直接截前 10 位
const dateOnly = (text) => String(text ?? '').slice(0, 10);

Page({
  data: {
    loading: true,
    member: null,
    cards: [],
    orders: [],
    activeTab: 'cards',
  },

  onLoad(options) {
    if (!requireLogin()) {
      return;
    }
    this.phone = options.phone ?? '';
    this.loadDetail();
  },

  // 一次拿齐资料、持卡、订单，切标签不再发请求
  async loadDetail() {
    if (!this.phone) {
      this.setData({ loading: false });
      return;
    }
    this.setData({ loading: true });
    try {
      const detail = await getMemberDetailByPhone(this.phone);
      const member = detail?.member ?? null;
      this.setData({
        member: member ? { ...member, createTime: dateOnly(member.createTime) } : null,
        cards: (detail?.cards ?? []).map(decorateCard),
        orders: (detail?.orders ?? []).map(decorateOrder),
      });
    } catch (error) {
      // 请求层已提示，按未找到处理，避免停在空白页
      this.setData({ member: null, cards: [], orders: [] });
    } finally {
      this.setData({ loading: false });
    }
  },

  switchTab(event) {
    const tab = event.currentTarget.dataset.tab;
    if (tab && tab !== this.data.activeTab) {
      this.setData({ activeTab: tab });
    }
  },
});
