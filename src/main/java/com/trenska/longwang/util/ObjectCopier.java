package com.trenska.longwang.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import net.sf.cglib.beans.BeanCopier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * BeanCopier特点:
 1. BeanCopier只拷贝名称和类型都相同的属性。
 2. 当目标类的setter数目比getter少时，创建BeanCopier会失败而导致拷贝不成功。
 */
public class ObjectCopier {
	private static Map<String, BeanCopier> beanCopierMap = new ConcurrentHashMap();

	private static String generateKey(Class<?> source, Class<?> target) {
		return source.toString() + target.toString();
	}

	public static void copyProperties(Object source, Object target) {
		String beanKey = generateKey(source.getClass(), target.getClass());
		BeanCopier copier = null;
		if (!beanCopierMap.containsKey(beanKey)) {
			copier = BeanCopier.create(source.getClass(), target.getClass(), false);
			beanCopierMap.put(beanKey, copier);
		} else {
			copier = beanCopierMap.get(beanKey);
		}
		copier.copy(source, target, null);
	}

	protected <T> T parseObject(Object obj, Class<T> clazz) {
		return parseObject(JSONObject.toJSONString(obj), clazz);
	}

	/**
	 * 转换对象(如果T里面还有自定义类，会被JSONObject覆盖)
	 *
	 * @param jsonString 源json串
	 * @param clazz      目标类
	 * @return
	 */
	protected <T> T parseObject(String jsonString, Class<T> clazz) {
		return JSONObject.parseObject(jsonString, clazz);
	}

	/**
	 * 转换对象(如果T里面还有自定义类，不会被JSONObject覆盖)
	 *
	 * @param obj  源对象
	 * @param type 目标对象 new TypeReference<T>(){}
	 * @return
	 */
	protected <T> T parseObject(Object obj, TypeReference<T> type) {
		//使用TypeReference可以让Bean里面的Bean不被JSONObject覆盖
		return parseObject(JSONObject.toJSONString(obj), type);
	}

	/**
	 * 转换对象(如果T里面还有自定义类，不会被JSONObject覆盖)
	 *
	 * @param type 目标对象 new TypeReference<T>(){}
	 * @return
	 */
	protected <T> T parseObject(String jsonStr, TypeReference<T> type) {
		//使用TypeReference可以让Bean里面的Bean不被JSONObject覆盖
		return JSONObject.parseObject(jsonStr, type, Feature.AllowComment);
	}

}
