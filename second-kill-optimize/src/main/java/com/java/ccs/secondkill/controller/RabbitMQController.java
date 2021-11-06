package com.java.ccs.secondkill.controller;

import com.java.ccs.secondkill.rabbitmq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author caocs
 * @date 2021/11/4
 */
@Controller
@RequestMapping("/rabbitmq")
public class RabbitMQController {
    @Autowired
    MQSender mqSender;

    /**
     * 测试消息队列
     */
    @RequestMapping("/send")
    @ResponseBody
    public void sendRabbitMQ() {
        mqSender.sendMessage("hello");
    }

}
