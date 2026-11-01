package com.cardmall.converter;

import com.cardmall.common.FileUrls;
import com.cardmall.entity.CardComment;
import com.cardmall.entity.MemberInfo;
import com.cardmall.vo.AdminCommentVO;
import com.cardmall.vo.CommentVO;
import com.cardmall.vo.MyCommentVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class CommentConverter {

    private CommentConverter() {
    }

    public static CommentVO toVO(CardComment comment, MemberInfo member) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setProductId(comment.getProductId());
        vo.setMemberNickname(member == null ? "" : member.getNickname());
        vo.setMemberAvatar(member == null ? "" : FileUrls.resolve(member.getAvatar()));
        vo.setContent(comment.getContent());
        vo.setImages(splitImages(comment.getImages()));
        vo.setReplyContent(comment.getReplyContent());
        vo.setReplyTime(comment.getReplyTime());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    public static MyCommentVO toMyVO(CardComment comment) {
        if (comment == null) {
            return null;
        }
        MyCommentVO vo = new MyCommentVO();
        vo.setId(comment.getId());
        vo.setProductId(comment.getProductId());
        vo.setProductName(comment.getProductName());
        vo.setContent(comment.getContent());
        vo.setImages(splitImages(comment.getImages()));
        vo.setStatus(comment.getStatus());
        vo.setReplyContent(comment.getReplyContent());
        vo.setReplyTime(comment.getReplyTime());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    public static AdminCommentVO toAdminVO(CardComment comment, MemberInfo member) {
        if (comment == null) {
            return null;
        }
        AdminCommentVO vo = new AdminCommentVO();
        vo.setId(comment.getId());
        vo.setProductId(comment.getProductId());
        vo.setProductName(comment.getProductName());
        vo.setMemberId(comment.getMemberId());
        vo.setMemberNickname(member == null ? "" : member.getNickname());
        vo.setMemberAvatar(member == null ? "" : FileUrls.resolve(member.getAvatar()));
        vo.setMemberPhone(member == null ? "" : member.getPhone());
        vo.setContent(comment.getContent());
        vo.setImages(splitImages(comment.getImages()));
        vo.setStatus(comment.getStatus());
        vo.setReplyContent(comment.getReplyContent());
        vo.setReplyTime(comment.getReplyTime());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    /** 库里是逗号分隔的 objectKey，出参统一转成地址列表 */
    public static List<String> splitImages(String images) {
        List<String> urls = new ArrayList<String>();
        if (images == null || images.trim().isEmpty()) {
            return urls;
        }
        for (String part : images.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                urls.add(FileUrls.resolve(value));
            }
        }
        return urls;
    }
}
