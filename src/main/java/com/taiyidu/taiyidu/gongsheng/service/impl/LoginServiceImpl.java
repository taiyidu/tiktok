package com.taiyidu.taiyidu.gongsheng.service.impl;

import com.taiyidu.taiyidu.gongsheng.exception.AccountLockedException;
import com.taiyidu.taiyidu.gongsheng.exception.AccountNotFoundException;
import com.taiyidu.taiyidu.gongsheng.exception.PasswordErrorException;
import com.taiyidu.taiyidu.gongsheng.mapper.UserMapper;
import com.taiyidu.taiyidu.gongsheng.pojo.dto.LoginInfo;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.User;
import com.taiyidu.taiyidu.gongsheng.service.LoginService;
import constant.MessageConstant;
import constant.StatusConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserMapper UserMapper;


    @Override
    public User login(LoginInfo loginInfo) {
        User user = UserMapper.getByUsername(loginInfo);
        String password = loginInfo.getPassword();
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在 这里使用的是自定义异常（账号不存在，详细属性封装到静态类中去了）
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //优化密码校验使用md5加密算法（因为md5加密不可逆，无法解密）
        //这里的实现思路中在传过来时是明文但数据库中是密文，所以统一校验加密后的密文
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return user;
    }
}
