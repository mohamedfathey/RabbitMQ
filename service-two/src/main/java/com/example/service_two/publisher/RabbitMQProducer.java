package com.example.service_two.publisher;

import com.example.service_two.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service

public class RabbitMQProducer {


    @Value("${rabbitmq.exchange.name}")
    private String exchange ;

    @Value("${rabbitmq.routing.json.name}")
    private String routingJsonKey ;

    private final RabbitTemplate rabbitTemplate ;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    public void sendJsonMessage(User user) {

        LOGGER.info(String.format("Json Message sent -> %S" , user.toString()));
        rabbitTemplate.convertAndSend(exchange,routingJsonKey,user);
    }
}
