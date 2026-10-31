package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long storeId;

    private String name;

    private String phone;

    /** BCrypt 密文，任何接口都不返回 */
    @JsonIgnore
    private String password;

    private String avatar;

    /** 1-老板，2-店长，3-店员 */
    private Integer role;

    /** 0-停用，1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
