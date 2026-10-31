/**
 * 核销记录：按日期筛选 + 触底加载，数据与后台核销记录同源
 * 作者：card-mall 创建日期：2026-10-31
 */
import { pageRecords } from '../../api/staffApi';
import { requireLogin } from '../../utils/auth';
import { decorateRecord } from '../../utils/dict';

const PAGE_SIZE = 10;

Page({
  data: {
    list: [],
    total: 0,
    loadingMore: false,
    finished: false,
    startDate: '',
    endDate: '',
  },

  // 用实例字段做并发闸门，避免触底与 onShow 同时翻页
  fetching: false,

  onShow() {
    if (!requireLogin()) {
      return;
    }
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/records/index' });
    }
    this.reload();
  },

  onPullDownRefresh() {
    this.reload().finally(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    this.loadMore();
  },

  async reload() {
    if (this.fetching) {
      return;
    }
    this.fetching = true;
    this.pageNum = 1;
    try {
      const page = await this.fetchPage(1);
      this.setData({ list: page.list, total: page.total, finished: page.list.length < PAGE_SIZE });
    } catch (error) {
      // 请求层已提示，保留上一次列表
    } finally {
      this.fetching = false;
    }
  },

  async loadMore() {
    if (this.fetching || this.data.finished) {
      return;
    }
    this.fetching = true;
    this.setData({ loadingMore: true });
    const nextPage = (this.pageNum ?? 1) + 1;
    try {
      const page = await this.fetchPage(nextPage);
      this.pageNum = nextPage;
      this.setData({
        list: [...this.data.list, ...page.list],
        total: page.total,
        finished: page.list.length < PAGE_SIZE,
      });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.fetching = false;
      this.setData({ loadingMore: false });
    }
  },

  async fetchPage(pageNum) {
    const { startDate, endDate } = this.data;
    const page = await pageRecords({ startDate, endDate, pageNum, pageSize: PAGE_SIZE });
    return { list: (page.list ?? []).map(decorateRecord), total: page.total ?? 0 };
  },

  onStartDate(event) {
    this.setData({ startDate: event.detail.value });
    this.reload();
  },

  onEndDate(event) {
    this.setData({ endDate: event.detail.value });
    this.reload();
  },

  clearDates() {
    this.setData({ startDate: '', endDate: '' });
    this.reload();
  },
});
