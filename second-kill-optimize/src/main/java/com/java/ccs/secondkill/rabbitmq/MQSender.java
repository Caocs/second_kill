package com.java.ccs.secondkill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author caocs
 * @date 2021/11/3
 */
@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 用来简单测试使用，发送消息
     */
    public void sendMessage(Object message) {
        log.info("发送消息：" + message);
        rabbitTemplate.convertAndSend("queue", message);
    }

    /**
     * 发送秒杀信息
     */
    public void sendSecondKillMessage(Object message) {
        log.info("发送消息：" + message);
        rabbitTemplate.convertAndSend("second-kill-topic-exchange", "second-kill.order",message);
    }

}
