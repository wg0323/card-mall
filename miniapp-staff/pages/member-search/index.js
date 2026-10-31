/**
 * 会员查询：手机号精确匹配，展示会员与其持卡
 * 作者：card-mall 创建日期：2026-10-31
 */
import { searchMember } from '../../api/staffApi';
import { requireLogin } from '../../utils/auth';
import { decorateCard } from '../../utils/dict';

// 注册时间只展示到日：后端给的是 yyyy-MM-dd HH:mm:ss，直接截前 10 位
const dateOnly = (text) => String(text ?? '').slice(0, 10);

Page({
  data: {
    phone: '',
    inputFocus: false,
    loading: false,
    searched: false,
    member: null,
    cards: [],
  },

  onLoad() {
    requireLogin();
  },

  onPhoneInput(event) {
    // 文本框键盘才有「搜索」键，非数字输入在这里过滤掉
    const phone = event.detail.value.replace(/\D/g, '');
    this.setData({ phone });
  },

  onInputFocus() {
    this.setData({ inputFocus: true });
  },

  onInputBlur() {
    this.setData({ inputFocus: false });
  },

  /** 清空输入与结果，并保持焦点方便继续输入 */
  clearPhone() {
    this.setData({ phone: '', inputFocus: true, searched: false, member: null, cards: [] });
  },

  /** 点会员卡片进详情页，详情页自己按手机号再拉一次 */
  goDetail() {
    const phone = this.data.member?.phone;
    if (!phone) {
      return;
    }
    wx.navigateTo({ url: `/pages/member-detail/index?phone=${encodeURIComponent(phone)}` });
  },

  async search() {
    const phone = this.data.phone.trim();
    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (this.data.loading) {
      return;
    }
    this.setData({ loading: true });
    try {
      const holder = await searchMember(phone);
      const member = holder?.member ?? null;
      this.setData({
        searched: true,
        // 查到人就清空输入框：号码已经在结果卡片上，留着只会挡住下一次输入；
        // 没查到则保留，方便直接改数字重试
        phone: member ? '' : phone,
        member: member ? { ...member, createTime: dateOnly(member.createTime) } : null,
        cards: (holder?.cards ?? []).map(decorateCard),
      });
    } catch (error) {
      // 请求层已提示，结果区按未找到处理
      this.setData({ searched: true, member: null, cards: [] });
    } finally {
      this.setData({ loading: false });
    }
  },
});
