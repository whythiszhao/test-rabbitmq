package com.example.demo.business;

import java.io.Serializable;

import lombok.Data;

@Data
public class CustomMessageBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1887814833785982597L;
	private String loginName;
	private String username;
	private Integer uid;
	
	/**
	 * 被序列化的对象应提供一个无参的构造函数，否则会抛出异常。
	 */
	public CustomMessageBean() {
	}
	
	public CustomMessageBean(String loginName, String username, Integer uid) {
		this.loginName = loginName;
		this.username = username;
		this.uid = uid;
	}
}
