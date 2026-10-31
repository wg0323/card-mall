/**
 * 自定义 tabBar：用 TDesign 图标组件，避免往仓库填 81x81 的 PNG
 * 作者：card-mall 创建日期：2026-10-31
 */
Component({
  data: {
    value: '/pages/home/index',
  },
  methods: {
    onChange(event) {
      const { value } = event.detail;
      if (value === this.data.value) {
        return;
      }
      this.setData({ value });
      wx.switchTab({ url: value });
    },
  },
});
