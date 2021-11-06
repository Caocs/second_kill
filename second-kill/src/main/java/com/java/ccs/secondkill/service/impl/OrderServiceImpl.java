package com.java.ccs.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.ccs.secondkill.mapper.OrderMapper;
import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.pojo.SecondKillGoods;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.service.ISecondKillGoodsService;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import com.java.ccs.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    ISecondKillGoodsService secondKillGoodsService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ISecondKillOrderService secondKillOrderService;

    /**
     * 执行秒杀操作
     * 1、秒杀商品表减库存
     * 2、生成订单信息
     * 3、生成秒杀订单信息
     */
    @Override
    public Order secondKillOrder(User user, GoodsVo goods) {
        // 秒杀商品表减库存
        SecondKillGoods secondKillGoods = secondKillGoodsService.getOne(new QueryWrapper<SecondKillGoods>().eq("goods_id", goods.getId()));
        secondKillGoods.setStockCount(secondKillGoods.getStockCount() - 1);
        secondKillGoodsService.updateById(secondKillGoods);

        // 生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(secondKillGoods.getSecondKillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateTime(new Date());
        orderMapper.insert(order);

        // 生成秒杀订单
        SecondKillOrder secondKillOrder = new SecondKillOrder();
        secondKillOrder.setGoodsId(goods.getId());
        secondKillOrder.setUserId(user.getId());
        secondKillOrder.setOrderId(order.getId()); // 当插入Order表之后自动返回主键
        secondKillOrderService.save(secondKillOrder);

        return order;
    }
}
