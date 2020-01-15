package com.trenska.longwang.util;

import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.entity.sys.SysConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 2019/6/14
 * 创建人:Owen
 */
public class SysUtil {

	public static int getEmpId() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String token = request.getHeader(Constant.TOKEN_NAME);
		if (StringUtils.isEmpty(token)) {
			return -1;
		}
		String[] strs = StringUtils.split(token, Constant.SPLITTER);
		if (strs == null || strs.length <= 1) {
			return -1;
		}
		String empIdStr = strs[1];
		return NumberUtils.toInt(empIdStr);
	}


	public static String getTokenInRedis(Optional<String> token) {
		String[] strs = StringUtils.split(token.get(), Constant.SPLITTER);
		if (strs == null || strs.length <= 1) {
			return null;
		}
		String empIdStr = strs[1];

		if (!StringUtil.isNumeric(empIdStr, false)) {
			return null;
		}

		RedisTemplate<String, String> redisTemplate =
				ApplicationContextHolder.getBean(Constant.REDIS_TEMPLATE_NAME);

		String tokenInRedis = redisTemplate.opsForValue().get(Constant.EMP_ID_IDENTIFIER.concat(empIdStr));

		return tokenInRedis;
	}

	/**
	 * 获取缓存进redis的账号对应的系统配置
	 *
	 * @return 账号对应的系统配置
	 */
	public static SysConfig getSysConfig(int empId) {
		// 从redis中获取SysConfig
		RedisTemplate<String, Object> jsonRedisTemplate =
				ApplicationContextHolder.getBean(Constant.REDIS_JSON_TEMPLATE_NAME);

		SysConfig sysConfig =
				(SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER.concat(String.valueOf(empId)));
		// 如果redis 出故障了或者缓存失效了则先获取数据库中的对应记录，如果数据库中没有对应的配置记录，则获取容器中的默认系统配置
		if (null == sysConfig) {
			sysConfig = new SysConfig().selectById(empId);
			if (null == sysConfig) {
				sysConfig = ApplicationContextHolder.getBean(Constant.SYS_CONFIG_IDENTIFIER.concat(String.valueOf(Constant.DEFAULT_CONFIG_NUMBER)));
			}
		}
		return sysConfig;
	}

	/**
	 * 获取缓存进redis的账号对应的系统配置 的数字保留位数
	 *
	 * @return 计算时小数保留位数
	 */
	public static int getSysConfigRetain() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		// 获取 token ，格式为 uuid + :: + empId
		String token = request.getHeader(Constant.TOKEN_NAME);

		//从token中获取empId
		String empIdInToken = getTokenInRedis(Optional.of(token));

		// 如果是一个无效的empId，则设置为系统默认配置对应的empId->10000
		if (!StringUtil.isNumeric(empIdInToken, false)) {
			empIdInToken = String.valueOf(Constant.DEFAULT_CONFIG_NUMBER);
		}

		// 从redis中获取SysConfig
		RedisTemplate<String, Object> jsonRedisTemplate =
				ApplicationContextHolder.getBean(Constant.REDIS_JSON_TEMPLATE_NAME);

		SysConfig sysConfig =
				(SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER.concat(empIdInToken));
		// 如果redis 出故障了则获取容器中的默认系统配置
		if (sysConfig == null) {
			sysConfig = new SysConfig().selectById(empIdInToken);
			if (sysConfig == null) {
				sysConfig = ApplicationContextHolder.getBean(Constant.SYS_CONFIG_IDENTIFIER.concat(String.valueOf(Constant.DEFAULT_CONFIG_NUMBER)));
			}
		}
		Integer retain = sysConfig.getRetain();
		return retain;
	}

	public static String assembleLastLoginIp() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String lastLoginIp =
				request.getRemoteHost().concat(" ").concat(request.getRemoteAddr()).concat(String.valueOf(request.getRemotePort()));
		return lastLoginIp;
	}

	/**
	 * 重新包装查询参数，处理数据权限和区域分组
	 *
	 * @param srcParams
	 * @param areaGrpMapper
	 * @param request
	 * @return
	 */
	public static Map<String, Object> dealDataPermAndAreaGrp(Map<String, Object> srcParams, AreaGrpMapper areaGrpMapper, HttpServletRequest request) {

		///////////////////////////////////// 处理数据权限 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Set<Integer> custIds = CustomerUtil.getCurrentUserDataAuth(request, areaGrpMapper);
		srcParams.put(Constant.CUST_IDS_LABEL, custIds);

		///////////////////////////////////// 处理区域分组 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Integer areaGrpId = (Integer) srcParams.get("areaGrpId");
		Set<Integer> areaGrpIds = new HashSet<>();
		if (NumberUtil.isIntegerUsable(areaGrpId)) {
			areaGrpIds.addAll(areaGrpMapper.selectSubAreaGrpIds(areaGrpId));
			srcParams.put("areaGrpIds", areaGrpIds);
		}
		return srcParams;
	}
}