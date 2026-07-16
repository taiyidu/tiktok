package com.taiyidu.taiyidu.gongsheng.service;

import com.taiyidu.taiyidu.gongsheng.pojo.dto.LoginInfo;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.User;

public interface LoginService{
    User login(LoginInfo loginInfo);
}
