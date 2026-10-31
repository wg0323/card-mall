// 状态映射按设计文档第 11 节统一在这里，页面里不允许各写一套
export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger';

export interface DictItem {
  label: string;
  type: TagType;
}

export const CARD_TYPE: Record<number, DictItem> = {
  1: { label: '计次型', type: 'primary' },
  2: { label: '期限型', type: 'warning' },
};

export const CARD_STATUS: Record<number, DictItem> = {
  0: { label: '已下架', type: 'info' },
  1: { label: '已上架', type: 'success' },
};

// 会员卡状态：已过期与已用完都用弱化色，不用红色，避免恐吓感
export const MEMBER_CARD_STATUS: Record<number, DictItem> = {
  1: { label: '可用', type: 'success' },
  2: { label: '已用完', type: 'info' },
  3: { label: '已过期', type: 'info' },
  4: { label: '已冻结', type: 'warning' },
};

export const PAY_STATUS: Record<number, DictItem> = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已关闭', type: 'info' },
  3: { label: '已退款', type: 'danger' },
};

export const PAY_TRANSACTION_STATUS: Record<number, DictItem> = {
  0: { label: '处理中', type: 'warning' },
  1: { label: '成功', type: 'success' },
  2: { label: '失败', type: 'danger' },
};

export const VERIFY_STATUS: Record<number, DictItem> = {
  1: { label: '已核销', type: 'primary' },
  2: { label: '已撤销', type: 'danger' },
};

export const STAFF_ROLE: Record<number, string> = {
  1: '老板',
  2: '店长',
  3: '店员',
};

export const STAFF_STATUS: Record<number, DictItem> = {
  0: { label: '已停用', type: 'info' },
  1: { label: '已启用', type: 'success' },
};

export function dictOf(map: Record<number, DictItem>, value: number | null | undefined): DictItem {
  if (value === null || value === undefined) {
    return { label: '-', type: 'info' };
  }
  return map[value] ?? { label: String(value), type: 'info' };
}
