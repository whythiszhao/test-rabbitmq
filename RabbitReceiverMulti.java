package com.example.demo.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;


/**
 * 在类上监听消息时，可对应地使用 @RabbitHandler 来处理不同类型的消息数据。
 * @author zhaoxia
 *
 * 2021年12月15日
 */
@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.local-second-queue.name:localhost.second.queue}")
public class RabbitReceiverMulti {

	private LinkedBlockingQueue<CustomMessageBean> queue4TrafficControl = new LinkedBlockingQueue<>(1);
	private ExecutorService threadPool = Executors.newSingleThreadExecutor(
			// google concurrency
//			new ThreadFactoryBuilder().setNameFormat("rabbitReceiver-pool-%d").build()
			);
	
	@RabbitHandler
	public void consumerObject(@Payload CustomMessageBean customMessageBean, 
			@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
		log.debug("receiver CustomMessageBean message [{}]", customMessageBean);
		try {
			queue4TrafficControl.put(customMessageBean);
		}catch(Exception e) {
			log.error("handle message has error.", e);
		}finally {
			channel.basicAck(deliveryTag, false);
		}
	}

	@RabbitHandler
	public void consumerString(@Payload String body, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
			Channel channel) throws IOException {
		log.debug("receiver string message [{}]", body);
		try {
			log.info("consumer msg [{}].", body);
			CustomMessageBean customMessageBean = JSON.parseObject(body, CustomMessageBean.class);
			queue4TrafficControl.put(customMessageBean);
		}catch(Exception e) {
			log.error("handle string message has error.", e);
		}finally {
			channel.basicAck(deliveryTag, false);
		}
	}

	@RabbitHandler
	public void consumerString(@Payload byte[] body, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
			Channel channel) throws IOException {
		log.debug("receiver byte message [{}]", body);
		try {
			log.info("consumer byte msg [{}].", body);
			CustomMessageBean customMessageBean = JSON.parseObject(body, CustomMessageBean.class);
			queue4TrafficControl.put(customMessageBean);
		}catch(Exception e) {
			log.error("handle byte message has error.", e);
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
