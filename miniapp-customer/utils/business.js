/**
 * 营业时间：HH:mm-HH:mm，结束时间早于开始时间视为跨天营业
 * 作者：card-mall 创建日期：2026-10-31
 */
const TIME_RANGE = /^(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2})$/;

const toMinutes = (hour, minute) => Number(hour) * 60 + Number(minute);

export const parseBusinessHours = (text) => {
  const match = TIME_RANGE.exec((text || '').trim());
  if (!match) {
    return null;
  }
  return { open: toMinutes(match[1], match[2]), close: toMinutes(match[3], match[4]) };
};

// 门店没配时间时按休息处理，避免误报营业中
export const isBusinessOpen = (text, date = new Date()) => {
  const range = parseBusinessHours(text);
  if (!range) {
    return false;
  }
  const minutes = date.getHours() * 60 + date.getMinutes();
  if (range.open === range.close) {
    return true;
  }
  if (range.open < range.close) {
    return minutes >= range.open && minutes < range.close;
  }
  return minutes >= range.open || minutes < range.close;
};
