package com.cardmall.service;

import com.cardmall.vo.VerifyPreviewVO;
import com.cardmall.vo.VerifyResultVO;

public interface VerifyService {

    /** 店员扫码后的预览：只读，不扣次不删码；失败会计入防盗刷计数 */
    VerifyPreviewVO preview(Long staffId, String token);

    /** 确认核销：先查 requestId 幂等，再消费核销码 */
    VerifyResultVO confirm(Long staffId, String token, String requestId, Integer times, String remark);
}
