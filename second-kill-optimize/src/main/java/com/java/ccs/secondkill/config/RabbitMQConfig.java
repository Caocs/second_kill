package com.java.ccs.secondkill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caocs
 * @date 2021/11/3
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        return new Queue("queue", true);
    }

    @Bean
    public Queue secondKillQueue() {
        return new Queue("second-kill-queue", true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange("second-kill-topic-exchange");
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(secondKillQueue()).to(topicExchange()).with("second-kill.#");
    }

}
