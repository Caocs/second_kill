package com.java.ccs.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
public interface ISecondKillOrderService extends IService<SecondKillOrder> {

    Long getSecondKillResult(User user, Long goodsId);
}
