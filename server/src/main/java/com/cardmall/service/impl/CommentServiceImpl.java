package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.FileUrls;
import com.cardmall.common.PageResult;
import com.cardmall.converter.CommentConverter;
import com.cardmall.dto.CommentCreateRequest;
import com.cardmall.entity.CardComment;
import com.cardmall.entity.CardProduct;
import com.cardmall.entity.MemberInfo;
import com.cardmall.mapper.CardCommentMapper;
import com.cardmall.mapper.CardOrderMapper;
import com.cardmall.service.CardProductService;
import com.cardmall.service.CommentService;
import com.cardmall.service.MemberService;
import com.cardmall.service.MessageService;
import com.cardmall.vo.AdminCommentVO;
import com.cardmall.vo.CommentVO;
import com.cardmall.vo.MyCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CardCommentMapper, CardComment> implements CommentService {

    private static final int MAX_IMAGES = 4;

    private static final int MAX_CONTENT_LENGTH = 500;

    private static final int MAX_IMAGE_KEY_LENGTH = 255;

    private static final int MAX_IMAGES_LENGTH = 1024;

    private final CardOrderMapper cardOrderMapper;

    private final CardProductService cardProductService;

    private final MemberService memberService;

    private final MessageService messageService;

    @Override
    public PageResult<CommentVO> pageByProduct(Long productId, int pageNum, int pageSize) {
        Page<CardComment> result = baseMapper.selectPage(
                new Page<CardComment>(pageNum, pageSize),
                new QueryWrapper<CardComment>()
                        .eq("product_id", productId)
                        .eq("status", CardComment.STATUS_VISIBLE)
                        .orderByDesc("create_time").orderByDesc("id"));
        Map<Long, MemberInfo> members = mapMembers(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal())
                .convert(comment -> CommentConverter.toVO(comment, members.get(comment.getMemberId())));
    }

    @Override
    public boolean canComment(Long memberId, Long productId, String orderNo) {
        if (StringUtils.hasText(orderNo)) {
            return cardOrderMapper.selectCommentableOrderNo(memberId, productId, orderNo.trim()) != null;
        }
        return cardOrderMapper.selectFirstCommentableOrderNo(memberId, productId) != null;
    }

    @Override
    public Long create(Long memberId, CommentCreateRequest request) {
        String content = normalizeContent(request.getContent());
        String images = joinImages(request.getImages());
        CardProduct product = cardProductService.getProductById(request.getProductId());
        if (product == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "卡产品不存在");
        }
        String orderNo = StringUtils.hasText(request.getOrderNo())
                ? cardOrderMapper.selectCommentableOrderNo(memberId, product.getId(), request.getOrderNo().trim())
                : cardOrderMapper.selectFirstCommentableOrderNo(memberId, product.getId());
        if (orderNo == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_ELIGIBLE, "购买后才能评论，或该订单已评论");
        }

        CardComment comment = new CardComment();
        comment.setProductId(product.getId());
        comment.setProductName(product.getName());
        comment.setMemberId(memberId);
        comment.setOrderNo(orderNo);
        comment.setContent(content);
        comment.setImages(images);
        comment.setStatus(CardComment.STATUS_VISIBLE);
        try {
            baseMapper.insert(comment);
        } catch (DuplicateKeyException e) {
            // 并发提交同一订单时，唯一索引兜底
            throw new BizException(ErrorCode.COMMENT_NOT_ELIGIBLE, "该订单已评论");
        }
        return comment.getId();
    }

    @Override
    public PageResult<MyCommentVO> pageByMember(Long memberId, int pageNum, int pageSize) {
        Page<CardComment> result = baseMapper.selectPage(
                new Page<CardComment>(pageNum, pageSize),
                new QueryWrapper<CardComment>()
                        .eq("member_id", memberId)
                        .ne("status", CardComment.STATUS_DELETED)
                        .orderByDesc("create_time").orderByDesc("id"));
        return PageResult.of(result.getRecords(), result.getTotal()).convert(CommentConverter::toMyVO);
    }

    @Override
    public void deleteByMember(Long memberId, Long id) {
        CardComment comment = baseMapper.selectById(id);
        if (comment == null || !comment.getMemberId().equals(memberId)
                || comment.getStatus() == CardComment.STATUS_DELETED) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND, "评论不存在");
        }
        baseMapper.update(null, new UpdateWrapper<CardComment>()
                .eq("id", id)
                .ne("status", CardComment.STATUS_DELETED)
                .set("status", CardComment.STATUS_DELETED));
    }

    @Override
    public PageResult<AdminCommentVO> pageForAdmin(Long productId, Integer status, int pageNum, int pageSize) {
        QueryWrapper<CardComment> qw = new QueryWrapper<CardComment>()
                .orderByDesc("create_time").orderByDesc("id");
        if (productId != null) {
            qw.eq("product_id", productId);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        Page<CardComment> result = baseMapper.selectPage(new Page<CardComment>(pageNum, pageSize), qw);
        Map<Long, MemberInfo> members = mapMembers(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal())
                .convert(comment -> CommentConverter.toAdminVO(comment, members.get(comment.getMemberId())));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != CardComment.STATUS_VISIBLE && status != CardComment.STATUS_HIDDEN)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "状态只能是 1 或 2");
        }
        requireNotDeleted(id);
        baseMapper.update(null, new UpdateWrapper<CardComment>()
                .eq("id", id)
                .ne("status", CardComment.STATUS_DELETED)
                .set("status", status));
    }

    @Override
    public void deleteByAdmin(Long id) {
        requireNotDeleted(id);
        baseMapper.update(null, new UpdateWrapper<CardComment>()
                .eq("id", id)
                .ne("status", CardComment.STATUS_DELETED)
                .set("status", CardComment.STATUS_DELETED));
    }

    @Override
    public void reply(Long id, Long staffId, String replyContent) {
        CardComment comment = requireNotDeleted(id);
        String content = replyContent == null ? "" : replyContent.trim();
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID, "回复内容最多 500 字");
        }
        if (content.isEmpty()) {
            // updateById 会忽略 null 字段，清空回复必须显式 set null
            baseMapper.update(null, new UpdateWrapper<CardComment>()
                    .eq("id", id)
                    .setSql("reply_content = null, reply_staff_id = null, reply_time = null"));
            return;
        }
        boolean firstReply = !StringUtils.hasText(comment.getReplyContent());
        baseMapper.update(null, new UpdateWrapper<CardComment>()
                .eq("id", id)
                .set("reply_content", content)
                .set("reply_staff_id", staffId)
                .set("reply_time", LocalDateTime.now()));
        if (firstReply) {
            // 一条评论只提醒一次；清空后再回复由消息表的唯一索引兜底
            messageService.createCommentReplyMessage(comment.getMemberId(), id, content);
        }
    }

    private CardComment requireNotDeleted(Long id) {
        CardComment comment = baseMapper.selectById(id);
        if (comment == null || comment.getStatus() == CardComment.STATUS_DELETED) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "评论不存在或已删除");
        }
        return comment;
    }

    private String normalizeContent(String content) {
        String value = content == null ? "" : content.trim();
        if (value.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论内容不能为空");
        }
        if (value.length() > MAX_CONTENT_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论内容最多 500 字");
        }
        return value;
    }

    private String joinImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "";
        }
        if (images.size() > MAX_IMAGES) {
            throw new BizException(ErrorCode.PARAM_INVALID, "最多上传 4 张图片");
        }
        List<String> keys = new ArrayList<String>();
        for (String image : images) {
            if (image == null || image.trim().isEmpty()) {
                continue;
            }
            String key = FileUrls.normalize(image.trim());
            if (key.length() > MAX_IMAGE_KEY_LENGTH) {
                throw new BizException(ErrorCode.PARAM_INVALID, "单张图片地址过长");
            }
            keys.add(key);
        }
        String joined = String.join(",", keys);
        if (joined.length() > MAX_IMAGES_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID, "图片地址总长度过长");
        }
        return joined;
    }

    private Map<Long, MemberInfo> mapMembers(List<CardComment> comments) {
        List<Long> memberIds = new ArrayList<Long>();
        for (CardComment comment : comments) {
            memberIds.add(comment.getMemberId());
        }
        return memberService.mapByIds(memberIds);
    }
}
