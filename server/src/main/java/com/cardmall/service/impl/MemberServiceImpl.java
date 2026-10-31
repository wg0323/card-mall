package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.FileUrls;
import com.cardmall.common.LoginResult;
import com.cardmall.common.PageResult;
import com.cardmall.converter.MemberConverter;
import com.cardmall.dto.MemberProfileUpdateRequest;
import com.cardmall.entity.MemberInfo;
import com.cardmall.mapper.MemberInfoMapper;
import com.cardmall.security.LoginType;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.MemberService;
import com.cardmall.service.TokenService;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.AdminMemberVO;
import com.cardmall.wx.WxAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberInfoMapper, MemberInfo> implements MemberService {

    private final WxAuthClient wxAuthClient;

    private final TokenService tokenService;

    private final MemberCardService memberCardService;

    private final VerifyQueryService verifyQueryService;

    /**
     * 这里故意不加 @Transactional：注册走唯一索引 + 捕获重复键后重新查询，
     * 每条语句各自自动提交，重新查询才能看到并发事务已提交的那一行。
     */
    @Override
    public LoginResult loginByCode(String code) {
        String openId = wxAuthClient.code2OpenId(code);
        MemberInfo member = findByOpenId(openId);
        if (member == null) {
            member = register(openId);
        }
        touchLastLogin(member.getId());
        return tokenService.issue(member.getId(), member.getNickname(), LoginType.MEMBER, null);
    }

    @Override
    public MemberInfo getProfile(Long memberId) {
        MemberInfo member = baseMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, "会员不存在");
        }
        return member;
    }

    @Override
    public MemberInfo findByOpenId(String openId) {
        return baseMapper.selectOne(new QueryWrapper<MemberInfo>().eq("open_id", openId));
    }

    /**
     * 店员按手机号查会员。手机号为空时不查，否则空串会把大量未绑手机号的会员选出来。
     */
    @Override
    public MemberInfo findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return baseMapper.selectOne(new QueryWrapper<MemberInfo>().eq("phone", phone.trim()));
    }

    @Override
    public PageResult<MemberInfo> pageMembers(String keyword, int pageNum, int pageSize) {
        QueryWrapper<MemberInfo> qw = new QueryWrapper<MemberInfo>().orderByDesc("id");
        if (keyword != null && !keyword.trim().isEmpty()) {
            String value = keyword.trim();
            qw.and(w -> w.eq("phone", value).or().likeRight("nickname", value));
        }
        Page<MemberInfo> result = baseMapper.selectPage(new Page<MemberInfo>(pageNum, pageSize), qw);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * 统计量按当前页的会员 id 批量取：只选需要的列，再在内存里按会员累加。
     * 一页十几个人，比为此新增两个 group by 的 mapper 方法更省事。
     */
    @Override
    public PageResult<AdminMemberVO> pageForAdmin(String keyword, int pageNum, int pageSize) {
        PageResult<MemberInfo> page = pageMembers(keyword, pageNum, pageSize);
        List<Long> memberIds = new ArrayList<Long>();
        for (MemberInfo member : page.getList()) {
            memberIds.add(member.getId());
        }
        Map<Long, Integer> cardCounts = memberCardService.countByMembers(memberIds);
        Map<Long, Integer> verifyTimes = verifyQueryService.sumVerifyTimesByMembers(memberIds);
        return page.convert(member -> MemberConverter.toAdminVO(member,
                cardCounts.getOrDefault(member.getId(), 0),
                verifyTimes.getOrDefault(member.getId(), 0)));
    }

    @Override
    public Map<Long, MemberInfo> mapByIds(Collection<Long> memberIds) {
        Map<Long, MemberInfo> members = new HashMap<Long, MemberInfo>();
        if (memberIds == null || memberIds.isEmpty()) {
            return members;
        }
        for (MemberInfo member : listByIds(memberIds)) {
            members.put(member.getId(), member);
        }
        return members;
    }

    @Override
    public String bindPhone(Long memberId, String code) {
        String phone = wxAuthClient.code2Phone(code);
        MemberInfo update = new MemberInfo();
        update.setId(memberId);
        update.setPhone(phone);
        baseMapper.updateById(update);
        return phone;
    }

    @Override
    public void updateProfile(Long memberId, MemberProfileUpdateRequest request) {
        MemberInfo update = new MemberInfo();
        update.setId(getProfile(memberId).getId());
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            update.setAvatar(FileUrls.normalize(request.getAvatar()));
        }
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            update.setNickname(request.getNickname().trim());
        }
        baseMapper.updateById(update);
    }

    private MemberInfo register(String openId) {
        MemberInfo member = new MemberInfo();
        member.setOpenId(openId);
        member.setNickname("");
        member.setAvatar("");
        member.setPhone("");
        try {
            baseMapper.insert(member);
            return member;
        } catch (DuplicateKeyException e) {
            // 同一用户并发首次登录，另一条线程已注册
            MemberInfo existing = findByOpenId(openId);
            if (existing == null) {
                throw e;
            }
            return existing;
        }
    }

    private void touchLastLogin(Long memberId) {
        MemberInfo update = new MemberInfo();
        update.setId(memberId);
        update.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(update);
    }
}
