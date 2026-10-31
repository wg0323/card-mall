/**
 * 核销码：全屏二维码 + 倒计时，到期自动换码
 * 作者：card-mall 创建日期：2026-10-30
 */
import drawQrcode from 'weapp-qrcode-canvas-2d';
import { createVerifyCode } from '../../api/cardApi';

Page({
  data: {
    loading: true,
    token: '',
    seconds: 0,
    total: 60,
    percent: 100,
  },

  onLoad(options) {
    this.cardId = options.id;
    this.issueCode();
  },

  onShow() {
    // 核销码页需要把屏幕调亮，否则收银台扫不出
    this.boostBrightness();
    // 从后台切回来时倒计时已停，重新取一个码
    if (this.data.token && this.data.seconds <= 0) {
      this.issueCode();
    }
  },

  onHide() {
    this.restoreBrightness();
    this.stopTimer();
  },

  onUnload() {
    this.restoreBrightness();
    this.stopTimer();
  },

  async issueCode() {
    this.stopTimer();
    this.setData({ loading: true });
    try {
      const result = await createVerifyCode(this.cardId);
      const total = result.expireIn ?? 60;
      this.setData({ token: result.token, total, seconds: total, percent: 100 });
      wx.nextTick(() => this.drawQr(result.token));
      this.startTimer();
    } catch (error) {
      // 请求层已提示，页面保留手动刷新按钮
    } finally {
      this.setData({ loading: false });
    }
  },

  drawQr(text) {
    const query = wx.createSelectorQuery();
    query
      .select('#qrcode')
      .fields({ node: true, size: true })
      .exec((res) => {
        const target = res?.[0];
        if (!target?.node) {
          return;
        }
        try {
          // 库内部会按 pixelRatio 设置 canvas 宽高并绘制，这里只传 CSS 宽度；绘制是异步的，必须接住 rejection
          Promise.resolve(
            drawQrcode({
              canvas: target.node,
              canvasId: 'qrcode',
              width: target.width,
              text,
            }),
          )
            .then(() => console.log('[qr] 绘制完成', target.width))
            .catch((error) => {
              console.error('[qr] 绘制失败', error?.message ?? error);
              wx.showToast({ title: '二维码绘制失败，请用数字码', icon: 'none' });
            });
        } catch (error) {
          // 同步异常（拿不到 2d 上下文等）同样回落到数字码
          console.error('[qr] 绘制异常', error?.message ?? error);
          wx.showToast({ title: '二维码绘制失败，请用数字码', icon: 'none' });
        }
      });
  },

  startTimer() {
    this.stopTimer();
    this.timer = setInterval(() => {
      const next = this.data.seconds - 1;
      if (next <= 0) {
        this.stopTimer();
        this.issueCode();
        return;
      }
      this.setData({ seconds: next, percent: Math.round((next / this.data.total) * 100) });
    }, 1000);
  },

  stopTimer() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },

  refreshCode() {
    this.issueCode();
  },

  /** 记下原亮度再拉到最亮；开发者工具不支持该 API 时静默跳过 */
  boostBrightness() {
    if (this.originalBrightness !== undefined) {
      return;
    }
    wx.getScreenBrightness({
      success: (res) => {
        this.originalBrightness = res.value;
        wx.setScreenBrightness({ value: 1, fail: () => {} });
      },
      fail: () => {},
    });
  },

  restoreBrightness() {
    if (this.originalBrightness === undefined) {
      return;
    }
    wx.setScreenBrightness({ value: this.originalBrightness, fail: () => {} });
    this.originalBrightness = undefined;
  },
});
