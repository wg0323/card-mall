package com.cardmall.converter;

import com.cardmall.common.FileUrls;
import com.cardmall.entity.MemberInfo;
import com.cardmall.vo.AdminMemberVO;
import com.cardmall.vo.MemberVO;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class MemberConverter {

    private MemberConverter() {
    }

    public static MemberVO toVO(MemberInfo member) {
        if (member == null) {
            return null;
        }
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setNickname(member.getNickname());
        vo.setAvatar(FileUrls.resolve(member.getAvatar()));
        vo.setPhone(member.getPhone());
        vo.setLastLoginTime(member.getLastLoginTime());
        vo.setCreateTime(member.getCreateTime());
        vo.setUpdateTime(member.getUpdateTime());
        return vo;
    }

    /** 后台会员列表：会员信息 + 持卡数与累计核销次数 */
    public static AdminMemberVO toAdminVO(MemberInfo member, int cardCount, int totalVerifyTimes) {
        if (member == null) {
            return null;
        }
        AdminMemberVO vo = new AdminMemberVO();
        vo.setId(member.getId());
        vo.setNickname(member.getNickname());
        vo.setAvatar(FileUrls.resolve(member.getAvatar()));
        vo.setPhone(member.getPhone());
        vo.setCardCount(cardCount);
        vo.setTotalVerifyTimes(totalVerifyTimes);
        vo.setLastLoginTime(member.getLastLoginTime());
        vo.setCreateTime(member.getCreateTime());
        return vo;
    }
}
