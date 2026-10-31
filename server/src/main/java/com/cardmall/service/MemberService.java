package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.LoginResult;
import com.cardmall.common.PageResult;
import com.cardmall.dto.MemberProfileUpdateRequest;
import com.cardmall.entity.MemberInfo;
import com.cardmall.vo.AdminMemberVO;

import java.util.Collection;
import java.util.Map;

public interface MemberService extends IService<MemberInfo> {

    LoginResult loginByCode(String code);

    MemberInfo getProfile(Long memberId);

    MemberInfo findByOpenId(String openId);

    /** 店员按手机号查会员；手机号为空时不查 */
    MemberInfo findByPhone(String phone);

    /** 手机号精确、昵称右模糊 */
    PageResult<MemberInfo> pageMembers(String keyword, int pageNum, int pageSize);

    /** 后台会员列表：在分页基础上补持卡数与累计核销次数 */
    PageResult<AdminMemberVO> pageForAdmin(String keyword, int pageNum, int pageSize);

    /** 按 id 批量查会员，订单列表补昵称用；空集合直接返回空 Map */
    Map<Long, MemberInfo> mapByIds(Collection<Long> memberIds);

    /** 用 wx.login 的 code 换手机号并落库 */
    String bindPhone(Long memberId, String code);

    /** 改头像与昵称，传空的那一项不动 */
    void updateProfile(Long memberId, MemberProfileUpdateRequest request);
}
