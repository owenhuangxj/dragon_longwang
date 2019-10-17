package com.trenska.longwang.util;

import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.sys.ResponseModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 2019/6/14
 * 创建人:Owen
 */
public class SysUtil {

	public static int getEmpIdInRedis(){
		RedisTemplate<String,String> redisTemplate = ApplicationContextHolder.getBean(Constant.REDIS_TEMPLATE_NAME);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String token = request.getHeader(Constant.TOKEN_NAME);
		String empIdInRedis = redisTemplate.opsForValue().get(token + Constant.EMP_ID_IDENTIFIER);
		if(StringUtil.isNumeric(empIdInRedis)){
			return NumberUtils.toInt(empIdInRedis);
		}
		return -1 ;
	}

	/**
	 * 获取存入redis中的登陆账号id
	 * @param request
	 * @return
	 */
	public static Integer getEmpIdInRedis( HttpServletRequest request){
		RedisTemplate<String,String> redisTemplate = ApplicationContextHolder.getBean(Constant.REDIS_TEMPLATE_NAME);
		Integer empId = null;
		String token = request.getHeader(Constant.TOKEN_NAME);
		if(StringUtils.isNotEmpty(token)){
			String empIdInRedis = redisTemplate.opsForValue().get(token.concat(Constant.EMP_ID_IDENTIFIER));
			if (StringUtil.isNumeric(empIdInRedis)){
				empId = NumberUtils.toInt(empIdInRedis);
			}

		}
		return empId;
	}

	public static ResponseModel getEmpId(HttpServletRequest request){
		RedisTemplate<String,String> redisTemplate = ApplicationContextHolder.getBean(Constant.REDIS_TEMPLATE_NAME);
		Integer empId = null;
		String token = request.getHeader(Constant.TOKEN_NAME);
		if(StringUtils.isNotEmpty(token)){
			String empIdInRedis = redisTemplate.opsForValue().get(token.concat(Constant.EMP_ID_IDENTIFIER));
			if (StringUtils.isNotEmpty(empIdInRedis)){
				empId = NumberUtils.toInt(empIdInRedis);
				Map<String,Integer> redisEmpId = new HashMap();
				redisEmpId.put("empId",empId);
				return ResponseModel.getInstance().succ(true).data(redisEmpId);
			}else{
				return ResponseModel.getInstance().succ(false).msg("登陆超时，请重新登陆");
			}
		}else{
			return ResponseModel.getInstance().succ(false).msg("登陆超时，请重新登陆");
		}
	}

	/**
	 * 重新包装查询参数，处理区域分组
	 * @param areaGrpMapper
	 * @return
	 */
	public static Set<Integer> dealAreaGrp( AreaGrpMapper areaGrpMapper ,int areaGrpId){
		Set<Integer> areaGrpIds = new HashSet<>();
		if (NumberUtil.isIntegerUsable(areaGrpId)) {
			return areaGrpMapper.selectSubAreaGrpIds(areaGrpId);
		}
		return new HashSet<>();
	}


	/**
	 * 重新包装查询参数，处理区域分组
	 * @param srcParams
	 * @param areaGrpMapper
	 * @return
	 */
	public static Map<String,Object> dealAreaGrp(Map<String,Object> srcParams, AreaGrpMapper areaGrpMapper){
		Integer areaGrpId = (Integer) srcParams.get("areaGrpId");
		Set<Integer> areaGrpIds = new HashSet<>();
		if (NumberUtil.isIntegerUsable(areaGrpId)) {
			areaGrpIds.addAll(areaGrpMapper.selectSubAreaGrpIds(areaGrpId));
			srcParams.put("areaGrpIds", areaGrpIds);
		}
		return srcParams;
	}


	/**
	 * 重新包装查询参数，处理数据权限和区域分组
	 * @param srcParams
	 * @param areaGrpMapper
	 * @param request
	 * @return
	 */
	public static Map<String,Object> dealDataPermAndAreaGrp(Map<String,Object> srcParams, AreaGrpMapper areaGrpMapper , HttpServletRequest request){

		///////////////////////////////////// 处理数据权限 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Set<Integer> custIds = CustomerUtil.getCurrentUserDataAuth(request, areaGrpMapper);
		srcParams.put("custIds", custIds);

		///////////////////////////////////// 处理区域分组 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Integer areaGrpId = (Integer) srcParams.get("areaGrpId");
		Set<Integer> areaGrpIds = new HashSet<>();
		if (NumberUtil.isIntegerUsable(areaGrpId)) {
			areaGrpIds.addAll(areaGrpMapper.selectSubAreaGrpIds(areaGrpId));
			srcParams.put("areaGrpIds", areaGrpIds);
		}
		return srcParams;
	}

	public static int getSysConfigRetain(){

		RedisTemplate<String,String> redisTemplate = ApplicationContextHolder.getBean(Constant.REDIS_TEMPLATE_NAME);

		RedisTemplate<String,Object> jsonRedisTemplate = ApplicationContextHolder.getBean(Constant.REDIS_JSON_TEMPLATE_NAME);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		String token = request.getHeader(Constant.TOKEN_NAME);

		String empIdInRedis = redisTemplate.opsForValue().get(token + Constant.EMP_ID_IDENTIFIER);

		SysConfig sysConfig = (SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER + empIdInRedis);

		Integer retain = sysConfig.getRetain();

		return retain;

	}

	public static int getSysConfigRetain(RedisTemplate<String,String> redisTemplate,RedisTemplate<String,Object> jsonRedisTemplate){

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		String token = request.getHeader(Constant.TOKEN_NAME);

		String empIdInRedis = redisTemplate.opsForValue().get(token + Constant.EMP_ID_IDENTIFIER);

		SysConfig sysConfig = (SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER + empIdInRedis);

		Integer retain = sysConfig.getRetain();

		return retain;

	}

}
