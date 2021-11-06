package com.java.ccs.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.ccs.secondkill.mapper.SecondKillOrderMapper;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Service
public class SecondKillOrderServiceImpl extends ServiceImpl<SecondKillOrderMapper, SecondKillOrder> implements ISecondKillOrderService {

    @Autowired
    private SecondKillOrderMapper secondKillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取秒杀结果
     *
     * @return 秒杀结果
     * 0:还有库存，排队中，（说明秒杀还没结束，暂时没有生成订单但是不代表秒杀失败，可能还在消息队列中也可能是秒杀失败）
     * -1:没有库存, （说明秒杀结束，没有）
     * >0:秒杀成功(返回订单号)
     */
    @Override
    public Long getSecondKillResult(User user, Long goodsId) {

        SecondKillOrder secondKillOrder = secondKillOrderMapper.selectOne(
                new QueryWrapper<SecondKillOrder>()
                        .eq("user_id", user.getId())
                        .eq("goods_id", goodsId)
        );
        if (null != secondKillOrder) {
            return secondKillOrder.getOrderId();
        }
        if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }
    }
}
