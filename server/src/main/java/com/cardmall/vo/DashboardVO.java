package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "工作台数据")
public class DashboardVO {

    @Schema(description = "今日核销次数")
    private long todayVerifyCount;

    @Schema(description = "今日新增会员数")
    private long todayNewMemberCount;

    @Schema(description = "今日销售额")
    private BigDecimal todaySaleAmount;

    @Schema(description = "近 7 天核销趋势")
    private List<DailyCountVO> last7Days;

    @Schema(description = "最近核销记录")
    private List<VerifyRecordVO> recentVerifies;

    @Data
    @Schema(description = "单日核销数")
    public static class DailyCountVO {

        @Schema(description = "日期")
        private String date;

        @Schema(description = "核销次数")
        private long count;
    }
}
