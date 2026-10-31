/**
 * 状态文案与配色：与后台 constants/dict.ts、客户小程序 utils/dict.js 保持一致
 * 作者：card-mall 创建日期：2026-10-31
 */
const CARD_STATUS = {
  1: { label: '可用', theme: 'success' },
  2: { label: '已用完', theme: 'default' },
  3: { label: '已过期', theme: 'default' },
  4: { label: '已冻结', theme: 'warning' },
};

const RECORD_STATUS = {
  1: { label: '已核销', theme: 'primary' },
  2: { label: '已撤销', theme: 'danger' },
};
const ROLE_LABEL = { 1: '老板', 2: '店长', 3: '店员' };

// 与后台 constants/dict.ts 的 PAY_STATUS 保持一致
const PAY_STATUS = {
  0: { label: '待支付', theme: 'warning' },
  1: { label: '已支付', theme: 'success' },
  2: { label: '已关闭', theme: 'default' },
  3: { label: '已退款', theme: 'danger' },
};

const fallback = { label: '-', theme: 'default' };

export const cardStatusOf = (status) => CARD_STATUS[status] ?? fallback;

export const recordStatusOf = (status) => RECORD_STATUS[status] ?? fallback;

export const payStatusOf = (status) => PAY_STATUS[status] ?? fallback;

export const roleLabel = (role) => ROLE_LABEL[role] ?? '店员';

// 'yyyy-MM-dd HH:mm:ss' 在 iOS 的 new Date 里解析失败，统一换成斜杠再比
export const parseTime = (text) => new Date(String(text ?? '').replace(/-/g, '/'));

// WXML 里不能调用函数，列表数据统一在这里补好展示字段
export const decorateCard = (card) => {
  const status = cardStatusOf(card.status);
  return {
    ...card,
    statusLabel: status.label,
    statusTheme: status.theme,
    timesText: card.cardType === 1 ? `${card.remainingTimes}` : '不限次',
    timesUnit: card.cardType === 1 ? '次' : '',
  };
};

export const decorateRecord = (record) => {
  const status = recordStatusOf(record.status);
  return {
    ...record,
    statusLabel: status.label,
    statusTheme: status.theme,
    timesText: record.verifyTimes ? `${record.verifyTimes} 次` : '不扣次',
  };
};

// 订单列表：金额补两位小数，下单时间去掉秒
export const decorateOrder = (order) => {
  const status = payStatusOf(order.payStatus);
  return {
    ...order,
    statusLabel: status.label,
    statusTheme: status.theme,
    amountText: Number(order.amount ?? 0).toFixed(2),
    createTimeText: String(order.createTime ?? '').slice(0, 16),
  };
};