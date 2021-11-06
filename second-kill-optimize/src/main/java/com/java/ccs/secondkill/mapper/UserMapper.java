package com.java.ccs.secondkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.ccs.secondkill.pojo.User;
import org.springframework.stereotype.Repository;

/**
 * @author ccs
 * @since 2021-10-21
 *
 * 使用@Repository，为了避免service层注入Mapper时，Idea提示报红。
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

}
