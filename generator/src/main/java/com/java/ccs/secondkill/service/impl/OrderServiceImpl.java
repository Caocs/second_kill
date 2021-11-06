package com.java.ccs.secondkill.service.impl;

import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.mapper.OrderMapper;
import com.java.ccs.secondkill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
