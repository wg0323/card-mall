package com.cardmall.service.impl;

import com.cardmall.service.DashboardService;
import com.cardmall.service.StoreService;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final JdbcTemplate jdbcTemplate;

    private final VerifyQueryService verifyQueryService;

    private final StoreService storeService;

    @Override
    public DashboardVO load() {
        Long storeId = storeService.getMainStore().getId();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        DashboardVO vo = new DashboardVO();
        vo.setTodayVerifyCount(verifyQueryService.todayCount(storeId));
        vo.setTodayNewMemberCount(queryLong(
                "select count(*) from member_info where create_time >= ?", todayStart));
        vo.setTodaySaleAmount(queryAmount(
                "select ifnull(sum(amount), 0) from card_order where pay_status = 1 and paid_time >= ?",
                todayStart));
        vo.setLast7Days(last7Days(today));
        vo.setRecentVerifies(verifyQueryService.page(storeId, null, null, null, 1, 10).getList());
        return vo;
    }

    /**
     * 有核销的日期逐日 group by 一次拿回来，缺的日期在 Java 里补零，不要查 7 次。
     */
    private List<DashboardVO.DailyCountVO> last7Days(LocalDate today) {
        LocalDate from = today.minusDays(6);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select date(verify_time) as d, count(*) as c from verify_record "
                        + "where status = 1 and verify_time >= ? group by date(verify_time)",
                from.atStartOfDay());

        Map<LocalDate, Long> counts = new HashMap<LocalDate, Long>();
        for (Map<String, Object> row : rows) {
            LocalDate date = ((Date) row.get("d")).toLocalDate();
            counts.put(date, ((Number) row.get("c")).longValue());
        }

        List<DashboardVO.DailyCountVO> list = new ArrayList<DashboardVO.DailyCountVO>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = from.plusDays(i);
            DashboardVO.DailyCountVO item = new DashboardVO.DailyCountVO();
            item.setDate(date.toString());
            item.setCount(counts.containsKey(date) ? counts.get(date) : 0L);
            list.add(item);
        }
        return list;
    }

    private long queryLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private BigDecimal queryAmount(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value;
    }
}
