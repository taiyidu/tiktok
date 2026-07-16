package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User{

    private Long id;//用户主键ID

    private String username;//登录账号，唯一

    private String password;//BCrypt加密后的密码，不存明文

    private String email;//邮箱

    private Integer status;//状态：1正常 0禁用

    private LocalDateTime createTime;//创建时间

    private Integer remain;
}
