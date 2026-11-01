/**
 * 写评论：文字 + 最多 4 张图片
 * 作者：card-mall 创建日期：2026-11-01
 */
import { createComment, uploadCommentImage } from '../../api/commentApi';
import { requireLogin } from '../../utils/auth';

const MAX_IMAGES = 4;
const MAX_CONTENT_LENGTH = 500;

const chooseMedia = (count) =>
  new Promise((resolve, reject) => {
    wx.chooseMedia({
      count,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: reject,
    });
  });

Page({
  data: {
    content: '',
    images: [],
    submitting: false,
    uploading: false,
    maxImages: MAX_IMAGES,
    maxContentLength: MAX_CONTENT_LENGTH,
  },

  onLoad(options) {
    if (!requireLogin()) {
      return;
    }
    this.productId = options.productId;
    this.orderNo = options.orderNo;
    if (!this.productId) {
      wx.showToast({ title: '缺少卡产品参数', icon: 'none' });
      wx.navigateBack();
    }
  },

  onContentInput(event) {
    this.setData({ content: event.detail.value });
  },

  async chooseImage() {
    if (this.data.uploading) {
      return;
    }
    const remaining = MAX_IMAGES - this.data.images.length;
    if (remaining <= 0) {
      wx.showToast({ title: `最多上传 ${MAX_IMAGES} 张图片`, icon: 'none' });
      return;
    }
    let files = [];
    try {
      const result = await chooseMedia(remaining);
      files = result.tempFiles ?? [];
    } catch (error) {
      if (!String(error?.errMsg ?? '').includes('cancel')) {
        wx.showToast({ title: '选择图片失败', icon: 'none' });
      }
      return;
    }
    if (!files.length) {
      return;
    }

    this.setData({ uploading: true });
    wx.showLoading({ title: '上传中', mask: true });
    const images = [...this.data.images];
    try {
      for (const file of files) {
        const url = await uploadCommentImage(file.tempFilePath);
        images.push(url);
        this.setData({ images: [...images] });
      }
    } catch (error) {
      // 请求层已提示；保留已上传成功的图片
    } finally {
      wx.hideLoading();
      this.setData({ uploading: false });
    }
  },

  removeImage(event) {
    const index = Number(event.currentTarget.dataset.index);
    const images = this.data.images.filter((item, itemIndex) => itemIndex !== index);
    this.setData({ images });
  },

  previewImage(event) {
    const index = Number(event.currentTarget.dataset.index);
    wx.previewImage({ current: this.data.images[index], urls: this.data.images });
  },

  async submit() {
    if (this.data.submitting || this.data.uploading || !this.productId) {
      return;
    }
    const content = this.data.content.trim();
    if (!content) {
      wx.showToast({ title: '请填写评论内容', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      const payload = {
        productId: Number(this.productId),
        content,
        images: this.data.images,
      };
      if (this.orderNo) {
        payload.orderNo = this.orderNo;
      }
      await createComment(payload);
      wx.showToast({ title: '评论成功', icon: 'success' });
      wx.navigateBack();
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ submitting: false });
    }
  },
});
