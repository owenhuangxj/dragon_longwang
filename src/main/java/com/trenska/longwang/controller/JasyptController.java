package com.trenska.longwang.controller;

import org.apache.commons.codec.StringEncoder;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2019/10/12
 * 创建人:Owen
 */
@RestController
public class JasyptController {

	@Autowired
	private StringEncryptor encryptor;

	@GetMapping("/encrypt/{key}")
	public String encrypt(@PathVariable String key){
		String encrypt = encryptor.encrypt(key);
		return encrypt;
	}

	@GetMapping("/decrypt/{key}")
	public String decrypt(@PathVariable String key){
		String decrypt = encryptor.decrypt(key);
		return decrypt;
	}
}
