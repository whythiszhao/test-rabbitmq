package com.example.demo.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费者
 * @author zhaoxia
 *
 * 2021年12月20日
 */
@Slf4j
@Component
public class RabbitReceiver {

	@Value("${rabbitmq.exchange.name:localhost.exchange}")
	private String exchange;
	@Value("${rabbitmq.queue.name:localhost.queue}")
	private String queue;
	
	private LinkedBlockingQueue<CustomMessageBean> queue4TrafficControl = new LinkedBlockingQueue<>(1);
	private ExecutorService threadPool = Executors.newSingleThreadExecutor(
			// google concurrency
//			new ThreadFactoryBuilder().setNameFormat("rabbitReceiver-pool-%d").build()
			);
    
//	@RabbitListener(bindings = @QueueBinding(
//			exchange = @Exchange(value="${rabbitmq.exchange.name:localhost.exchange}", durable = "true", type = "topic"),
//			value = @Queue(value = "${rabbitmq.queue.name:localhost.queue}", durable = "true"),
//			key = "all.key.*"
//			))
	@RabbitListener(queues = "localhost.queue")
	public void consumer(@Payload String json, 
			@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
		log.debug("receiver message [{}]", json);
		try {
			CustomMessageBean customMessageBean = JSON.parseObject(json, CustomMessageBean.class);
			queue4TrafficControl.put(customMessageBean);
		}catch(Exception e) {
			log.error("handle message has error.", e);
		}finally {
			channel.basicAck(deliveryTag, false);
		}
	}
	
	@PostConstruct
    public void consumeMessage() throws InterruptedException {
    	threadPool.execute(() -> {
			List<CustomMessageBean> container = new ArrayList<>();
			int timeout = 100;
			while(true) {
				try {
					CustomMessageBean customMessageBean = queue4TrafficControl.poll(timeout, TimeUnit.MILLISECONDS);
					if(customMessageBean == null) {
						if(!container.isEmpty()) {
							log.info("container has less 20.");
							container.clear();
						}
						timeout = 1000;
						continue;
					}
					if(container.size() >= 20) {
						log.info("container has 20.");
						container.clear();
					}
					container.add(customMessageBean);
					timeout = 100;
				}catch(Exception e) {
					log.error("consume message error.", e);
				}
			}
    	});
	}
}
