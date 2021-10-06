package com.trenska.longwang.config;

import com.sun.crypto.provider.SunJCE;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2021/9/1
 * 创建人:Owen
 */
@Configuration
public class SecurityConfiguration {

	@Value("${jasypt.encryptor.password}")
	private String password;
	@Bean
	public SimpleStringPBEConfig cryptor() {
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPoolSize(1);
		config.setPassword(password);
		/* JCE:Java Cryptography Extension Java加密扩展 */
		config.setProvider(new SunJCE());
		config.setStringOutputType("base64");
		config.setKeyObtentionIterations(1000);
		/* PBE:Password Based Encryption 基于口令加密
		 * MD5:Message-Digest Algorithm 消息摘要算法
		 * -> 一种被广泛使用的密码散列函数，可以产生出一个128位（16字节）的散列值（hash value），用于确保信息传输完整一致。
		 * DES:Data Encryption Standard 即数据加密标准，是一种使用密钥加密的块算法
		 * */
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setSaltGenerator(new RandomSaltGenerator());
		return config;
	}
}
