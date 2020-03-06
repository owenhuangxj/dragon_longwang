package com.trenska.longwang.util;

import com.sun.crypto.provider.SunJCE;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;

public class JasyptUtil {
	/**
	 * 加密
	 *
	 * @param password  密盐
	 * @param plaintext 明文
	 * @return 密文
	 */
	public static String encypt(String password, String plaintext) {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		encryptor.setConfig(cryptor(password));
		String ciphertext = encryptor.encrypt(plaintext);
		return ciphertext;
	}

	/**
	 * 解密
	 *
	 * @param password   密盐
	 * @param ciphertext 密文
	 * @return 明文
	 */
	public static String decrypt(String password, String ciphertext) {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		encryptor.setConfig(cryptor(password));
		String plaintext = encryptor.decrypt(ciphertext);
		return plaintext;
	}

	public static SimpleStringPBEConfig cryptor(String password) {
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

	public static void main(String[] args) {
		System.out.println(-1 | -0);
		System.out.println(1 << 3);
		System.out.println(1 >> Integer.SIZE - 1);
		System.out.println(1 << Integer.SIZE - 3);
		System.out.println(-1 << 29);
	}
}