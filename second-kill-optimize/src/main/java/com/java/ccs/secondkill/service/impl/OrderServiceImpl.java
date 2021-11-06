package com.java.ccs.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.ccs.secondkill.exception.GlobalException;
import com.java.ccs.secondkill.mapper.OrderMapper;
import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.pojo.SecondKillGoods;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.service.ISecondKillGoodsService;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import com.java.ccs.secondkill.util.MD5Util;
import com.java.ccs.secondkill.util.UuidUtil;
import com.java.ccs.secondkill.vo.GoodsVo;
import com.java.ccs.secondkill.vo.OrderDetailVo;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据user和goodsId，创建对应的随机uuid。
     * 在redis中缓存。
     * 把这串uuid放在秒杀商品的链接中，在执行秒杀时校验。
     */
    @Override
    public String createSecondKillPath(User user, Long goodsId) {
        String pathUuid = MD5Util.md5(UuidUtil.uuid() + "123456");
        // 把生成的随机Code缓存在redis中。执行秒杀时做对比校验。
        redisTemplate.opsForValue()
                .set("secondKillPath:" + user.getId() + ":" + goodsId, pathUuid, 60, TimeUnit.SECONDS);
        return pathUuid;
    }

    /**
     * 把秒杀接口传递的path和之前创建连接时生成的path进行对比。
     * 判断是否相等。
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String pathFromRedis = (String) redisTemplate.opsForValue()
                .get("secondKillPath:" + user.getId() + ":" + goodsId);
        return path.equals(pathFromRedis);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captchaResult) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(captchaResult)) {
            return false;
        }
        String captchaFromRedis = (String) redisTemplate.opsForValue()
                .get("captcha:" + user.getId() + ":" + goodsId);
        return captchaResult.equals(captchaFromRedis);
    }

    /**
     * 执行秒杀操作
     * 1、秒杀商品表减库存
     * 2、生成订单信息
     * 3、生成秒杀订单信息
     * <p>
     * （1）解决超卖问题：（利用排它锁）
     * update t_second_kill_goods set stock_count = stock_count-1 where goods_id={} and stock_count>0
     * （2）避免同一个用户多次秒杀该商品：
     * 建立goods_id-user_id的唯一索引。
     */
    @Transactional
    @Override
    public Order secondKillOrder(User user, GoodsVo goods) {
        // 秒杀商品表减库存
        SecondKillGoods secondKillGoods = secondKillGoodsService.getOne(
                new QueryWrapper<SecondKillGoods>()
                        .eq("goods_id", goods.getId())
        );
        // secondKillGoodsService.updateById(secondKillGoods);
        // 解决超卖问题
        boolean secondKillResult = secondKillGoodsService.update(
                new UpdateWrapper<SecondKillGoods>()
                        .setSql("stock_count = stock_count-1")
                        .eq("goods_id", goods.getId())
                        .gt("stock_count", 0)
        );
        if (secondKillGoods.getStockCount() < 1) {
            // 做标记，在页面端轮训的时候，判断库存状态。
            redisTemplate.opsForValue().set("isStockEmpty:" + goods.getId(), 0);
            return null;
        }
        if (!secondKillResult) {
            // 秒杀失败
            return null;
        }
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

        // 如果秒杀成功，则把用户及订单goodsId缓存。用于判断是否重复抢购。
        redisTemplate.opsForValue()
                .set("order:" + user.getId() + ":" + goods.getId(), secondKillOrder);

        return order;
    }

    @Override
    public OrderDetailVo getOrderDetail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(ResponseBeanEnum.ERROR);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(order);
        orderDetailVo.setGoodsVo(goodsVo);
        return orderDetailVo;
    }


}
