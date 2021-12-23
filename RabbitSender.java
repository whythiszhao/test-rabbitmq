package com.example.demo.business;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Order(1)
@Service
public class RabbitSender {
	@Autowired
	private RabbitAdmin rabbitAdmin;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Value("${rabbitmq.exchange.name:localhost.exchange}")
	private String exchangeName;
	@Value("${rabbitmq.local-queue.name:localhost.queue}")
	private String queueName;
	@Value("${rabbitmq.local-second-queue.name:localhost.second.queue}")
	private String queueSecondName;
	
//	@PostConstruct
	public void send() {
		TopicExchange exchange = new TopicExchange(exchangeName);
		rabbitAdmin.declareExchange(exchange);
        Queue queue=new Queue(queueName,true);
		rabbitAdmin.declareQueue(queue);
        Queue secondQueue=new Queue(queueSecondName,true);
		rabbitAdmin.declareQueue(secondQueue);
		
		int j = 1;
		for(int i = 70000; i < 70050; i++) {
			CustomMessageBean customMessageBean = new CustomMessageBean("USR_DPL_test"+j, "加工测试用户"+j, i);
			rabbitTemplate.convertAndSend(exchangeName, "all.key.202112", customMessageBean);
			CustomMessageBean customMessageBean2 = new CustomMessageBean("NMIC_DPL_test"+j, "产品加工测试用户"+j, i);
			rabbitTemplate.convertAndSend(exchangeName, "second.key.202112", customMessageBean2);
			j++;
		}
	}
}
