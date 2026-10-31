/**
 * 手机号门禁：未授权时全屏覆盖，不给绕过入口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { bindPhone } from '../../api/authApi';
import { ensureLogin } from '../../utils/auth';

Component({
  data: {
    binding: false,
  },

  methods: {
    async onGetPhone(event) {
      const code = event.detail?.code;
      if (!code) {
        wx.showToast({ title: '需要授权手机号才能使用', icon: 'none' });
        return;
      }
      if (this.data.binding) {
        return;
      }
      this.setData({ binding: true });
      try {
        // 登录只由这次点击触发：没有登录态先换 token，再绑手机号
        await ensureLogin();
        await bindPhone(code);
        this.triggerEvent('success');
      } catch (error) {
        // 请求层已提示
      } finally {
        this.setData({ binding: false });
      }
    },
  },
});
