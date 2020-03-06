package com.trenska.longwang.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 2020/3/1
 * 创建人:Owen
 */
public class CryptographyUtil {
	public static final String encrypt(String plainText) {
		Key secretKey = getKey();
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] p = plainText.getBytes(StandardCharsets.UTF_8);
			byte[] result = cipher.doFinal(p);
			BASE64Encoder encoder = new BASE64Encoder();
			String encoded = encoder.encode(result);
			return encoded;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static final String decrypt(String cipherText) {
		Key secretKey = getKey();
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] c = decoder.decodeBuffer(cipherText);
			byte[] result = cipher.doFinal(c);
			String plainText = new String(result, "UTF-8");
			return plainText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 提供基于口令的加密功能.
	 *
	 * @param plainText 明文
	 * @return 加密后的密文.
	 */
	public static final String pbeEncrypt(String plainText) {
		Key pbeSecretKey = getPBEKey();
		PBEParameterSpec pbeParamSpec = getParamSpec();
		try {
			Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.ENCRYPT_MODE, pbeSecretKey, pbeParamSpec);
			byte[] p = plainText.getBytes(StandardCharsets.UTF_8);
			byte[] result = cipher.doFinal(p);
			BASE64Encoder encoder = new BASE64Encoder();
			String encoded = encoder.encode(result);
			return encoded;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 提供基于口令的解密功能.
	 *
	 * @param cipherText 密文
	 * @return 解密后的明文.
	 */
	public static final String pbeDecrypt(String cipherText) {
		Key pbeSecretKey = getPBEKey();
		PBEParameterSpec pbeParamSpec = getParamSpec();
		try {
			Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.DECRYPT_MODE, pbeSecretKey, pbeParamSpec);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] c = decoder.decodeBuffer(cipherText);
			byte[] result = cipher.doFinal(c);
			String plainText = new String(result, StandardCharsets.UTF_8);
			return plainText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	private static final Key getKey() {
		KeyGenerator generator = null;
		try {
			generator = KeyGenerator.getInstance("AES/CBC/PKCS7Padding");
//			generator = KeyGenerator.getInstance("AES/CBC/PKCS5PADDING");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		generator.init(256);
		return generator.generateKey();
	}

	/**
	 * 获取PBE算法的密钥. 注意PBE密钥由用户提供的口令构造出来的,
	 * 用户提供的口令务必使用char数组, 而不能使用字符串, 字符数
	 * 组用完即清空.
	 *
	 * @return PBE算法的密钥.
	 */
	private static final Key getPBEKey() {
		// TODO come from db or System.in, NOTE: MUST be char array, not java.lang.String
		char[] pwd = {'%', '_', 'A', 's', '9', 'K'};
		SecretKey pbeKey;
		PBEKeySpec pbeKeySpec = new PBEKeySpec(pwd);
		try {
			SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			pbeKey = keyFac.generateSecret(pbeKeySpec);
			return pbeKey;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Arrays.fill(pwd, ' ');
		}
	}
	/**
	 * 获取PBE的算法参数, 涉及salt和iterate count两个参数.
	 *
	 * @return PBE的算法参数.
	 */
	private static final PBEParameterSpec getParamSpec() {
		byte[] salt = { (byte) 0xab, (byte) 0x58, (byte) 0xa1, (byte) 0x8c,
				(byte) 0x3e, (byte) 0xc8, (byte) 0x9d, (byte) 0x7a };
		int count = 20;
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, count);
		return paramSpec;
	}
}
