/**
 * 核销成功：整页状态反馈，店员不看屏幕底部也能一眼确认
 * 作者：card-mall 创建日期：2026-10-31
 */
Page({
  data: {
    cardName: '',
    timesText: '',
    remainingText: '',
    verifyTime: '',
    usedUp: false,
  },

  onLoad(options) {
    const times = Number(options.times ?? 0);
    const remaining = options.remaining ?? '';
    this.setData({
      cardName: decodeURIComponent(options.cardName ?? ''),
      verifyTime: decodeURIComponent(options.verifyTime ?? ''),
      timesText: times > 0 ? `本次扣 ${times} 次` : '本次不扣次',
      remainingText: remaining === '' ? '' : `剩余 ${remaining} 次`,
      usedUp: remaining === '0',
    });
  },

  continueScan() {
    wx.reLaunch({ url: '/pages/home/index' });
  },
});
