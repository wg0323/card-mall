// 与后端 com.cardmall.vo / dto 一一对应，字段名不改，避免前后端各叫一套

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface LoginResult {
  token: string;
  id: number;
  name: string;
  refreshToken: string;
}

export interface TokenPair {
  token: string;
  refreshToken: string;
  tokenExpireIn: number;
  refreshExpireIn: number;
}

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface CardProductVO {
  id: number;
  name: string;
  cardType: number;
  bizTag: string;
  price: number;
  unitPrice: number;
  totalTimes: number;
  validDays: number;
  coverImage: string;
  applicableService: string;
  purchaseNotice: string;
  status: number;
  sort: number;
  createTime: string;
  updateTime: string;
}

export interface CardProductSaveRequest {
  name: string;
  cardType: number;
  bizTag: string;
  price: number;
  unitPrice: number;
  totalTimes: number;
  validDays: number;
  coverImage: string;
  applicableService: string;
  purchaseNotice: string;
  status: number;
  sort: number;
}

export interface DailyCountVO {
  date: string;
  count: number;
}

export interface CardOrderVO {
  id: number;
  orderNo: string;
  storeId: number;
  memberId: number;
  memberNickname: string | null;
  memberPhone: string | null;
  productId: number;
  productName: string;
  amount: number;
  payChannel: string;
  payStatus: number;
  paidTime: string | null;
  closeTime: string | null;
  createTime: string;
  updateTime: string;
}

export interface PayTransactionVO {
  id: number;
  orderNo: string;
  channel: string;
  transactionId: string;
  amount: number;
  status: number;
  finishTime: string | null;
  createTime: string;
}

export interface CardOrderDetailVO {
  order: CardOrderVO;
  payTransactions: PayTransactionVO[];
}

export interface VerifyRecordVO {
  id: number;
  recordNo: string;
  cardId: number;
  cardName: string;
  memberId: number;
  memberNickname: string;
  memberPhone: string;
  staffId: number;
  staffName: string;
  verifyTimes: number;
  remainingBefore: number | null;
  remainingAfter: number | null;
  status: number;
  remark: string;
  verifyTime: string;
}

export interface DashboardVO {
  todayVerifyCount: number;
  todayNewMemberCount: number;
  todaySaleAmount: number;
  last7Days: DailyCountVO[];
  recentVerifies: VerifyRecordVO[];
}

export interface MemberVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  lastLoginTime: string | null;
  createTime: string;
  updateTime: string;
}

export interface AdminMemberVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  cardCount: number;
  totalVerifyTimes: number;
  lastLoginTime: string | null;
  createTime: string;
}

export interface MemberCardVO {
  id: number;
  cardNo: string;
  memberId: number;
  productId: number;
  orderNo: string;
  cardName: string;
  cardType: number;
  totalTimes: number;
  remainingTimes: number;
  validStart: string;
  validEnd: string;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface MemberDetailVO {
  member: MemberVO;
  cards: MemberCardVO[];
  verifies: VerifyRecordVO[];
  orders: CardOrderVO[];
}

export interface StaffVO {
  id: number;
  storeId: number;
  name: string;
  phone: string;
  avatar: string;
  role: number;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface StaffCreateRequest {
  name: string;
  phone: string;
  role: number;
}

export interface StoreVO {
  id: number;
  name: string;
  logo: string;
  phone: string;
  address: string;
  longitude: number | null;
  latitude: number | null;
  businessHours: string;
}

export interface StoreSaveRequest {
  name: string;
  logo: string;
  phone: string;
  address: string;
  longitude: number | null;
  latitude: number | null;
  businessHours: string;
}
