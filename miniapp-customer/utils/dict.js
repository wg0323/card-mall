/**
 * 状态文案与配色：与后台 constants/dict.ts、设计文档第 11 节保持一致
 * 作者：card-mall 创建日期：2026-10-30
 */
const MEMBER_CARD_STATUS = {
  1: { label: '可用', theme: 'success' },
  2: { label: '已用完', theme: 'default' },
  3: { label: '已过期', theme: 'default' },
  4: { label: '已冻结', theme: 'warning' },
};

const PAY_STATUS = {
  0: { label: '待支付', theme: 'warning' },
  1: { label: '已支付', theme: 'success' },
  2: { label: '已关闭', theme: 'default' },
  3: { label: '已退款', theme: 'danger' },
};

const fallback = { label: '-', theme: 'default' };

export const cardStatusOf = (status) => MEMBER_CARD_STATUS[status] ?? fallback;

export const payStatusOf = (status) => PAY_STATUS[status] ?? fallback;

// WXML 里不能调用函数，列表数据统一在这里补好展示字段
export const decorateCard = (card) => {
  const status = cardStatusOf(card.status);
  return {
    ...card,
    statusLabel: status.label,
    statusTheme: status.theme,
    timesText: card.cardType === 1 ? `${card.remainingTimes}` : '不限次',
    timesUnit: card.cardType === 1 ? '次' : '',
    cardNoTail: (card.cardNo ?? '').slice(-4),
  };
};

export const decorateOrder = (order) => {
  const status = payStatusOf(order.payStatus);
  return {
    ...order,
    payStatusLabel: status.label,
    payStatusTheme: status.theme,
    amountText: Number(order.amount ?? 0).toFixed(2),
  };
};
