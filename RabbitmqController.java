package com.example.demo.business;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/rabbit")
@Slf4j
public class RabbitmqController {
	
	@Autowired
	private RabbitSender rabbitSender;
	
	@RequestMapping(value = "/send", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> send(){
		log.info("send rabbit mq...");
		rabbitSender.send();
		Map<String, Object> map = new HashMap<>();
		map.put("success", true);
		map.put("msg", "发送成功。");
		return map;
	}
}
