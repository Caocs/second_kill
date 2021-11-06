package com.java.ccs.secondkill.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.java.ccs.secondkill.pojo.SecondKillMessage;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author caocs
 * @date 2021/11/4
 */
@Slf4j
@Service
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;


    @RabbitListener(queues = "queue")
    public void receiveMessage(String message) {
        log.info("接收消息：" + message);
    }

    @RabbitListener(queues = "second-kill-queue")
    public void receiveSecondKillMessage(String message) {
        log.info("接收消息：" + message);
        SecondKillMessage secondKillMessage = JSON.parseObject(message, SecondKillMessage.class);
        Long goodsId = secondKillMessage.getGoodsId();
        User user = secondKillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        // 判断是否重复抢购
        SecondKillOrder secondKillOrder = (SecondKillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (secondKillOrder != null) {
            return;
        }
        // 执行下单
        orderService.secondKillOrder(user, goodsVo);
    }

}
