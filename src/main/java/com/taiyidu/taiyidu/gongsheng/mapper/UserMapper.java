package com.taiyidu.taiyidu.gongsheng.mapper;

import com.taiyidu.taiyidu.gongsheng.pojo.dto.LoginInfo;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User getByUsername(LoginInfo loginInfo);

    void updateUser(User user);

    User getById(Long id);
}
