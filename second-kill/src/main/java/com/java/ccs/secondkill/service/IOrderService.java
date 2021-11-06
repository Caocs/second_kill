package com.java.ccs.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.vo.GoodsVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
public interface IOrderService extends IService<Order> {

    Order secondKillOrder(User user, GoodsVo goods);

}
