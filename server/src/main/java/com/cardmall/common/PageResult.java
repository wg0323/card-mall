package com.cardmall.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> list;

    @Schema(description = "总条数")
    private long total;

    public static <T> PageResult<T> of(List<T> list, long total) {
        PageResult<T> r = new PageResult<T>();
        r.list = list;
        r.total = total;
        return r;
    }

    /** 分页出参由 Entity 转 VO 用，避免 Controller 里手写 list 循环 */
    public <R> PageResult<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> target = new ArrayList<R>();
        if (list != null) {
            for (T item : list) {
                target.add(mapper.apply(item));
            }
        }
        return PageResult.of(target, total);
    }
}
