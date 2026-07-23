package com.taiyidu.taiyidu.gongsheng.controller;

import cn.hutool.core.date.DateTime;
import com.taiyidu.taiyidu.gongsheng.pojo.dto.LoginInfo;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.User;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.LoginResultVo;
import com.taiyidu.taiyidu.gongsheng.result.Result;
import com.taiyidu.taiyidu.gongsheng.service.LoginService;
import com.taiyidu.taiyidu.gongsheng.config.JwtProperties;
import com.taiyidu.taiyidu.gongsheng.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/login")
public class LoginContorller {
    @Autowired
    private LoginService loginService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 实现登录的接口
     * @param loginInfo 登录信息
     * @return token + username
     */
    @PostMapping
    public Result Login(@RequestBody LoginInfo loginInfo){
        log.info("[登录接口] 正在登录... 用户名:{}",loginInfo.getUsername());
        log.info("[登录接口] 正在登录... 密码:{}",loginInfo.getPassword());
        log.info("[登录接口] 登录时间:{}", DateTime.now());
        User user = loginService.login(loginInfo);
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = JwtUtil.generateJwt(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        LoginResultVo loginResultVo = new LoginResultVo(token, user.getUsername());
        log.info("[登录接口] 登录成功... 用户:{}",user);
        return Result.success(loginResultVo);
    }
}
