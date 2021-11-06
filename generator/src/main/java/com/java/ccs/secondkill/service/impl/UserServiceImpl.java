package com.java.ccs.secondkill.service.impl;

import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.mapper.UserMapper;
import com.java.ccs.secondkill.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ccs
 * @since 2021-10-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
