package com.trenska.longwang.service.customer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.model.customer.AreaGrpModel;
import com.trenska.longwang.model.sys.CommonResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 */
public interface IAreaGrpService extends IService<AreaGrp> {
	/**
	 * 将areaId作为查询下级CustArea的pid，查询出此areaId对应的所有下级区域
	 */
	List<AreaGrp> getSubAreaGrp(Integer areaId);
	Page<AreaGrp> getAreaGrpPage(Page page);
	Page<AreaGrp> getThirdClassAreaGrpPage(Page page);
	CommonResponse addSubAreaGrp(AreaGrp area);
	CommonResponse removeAreaGrp(Integer areaId, Integer areaDeep);
	Boolean updateAreaGrp(AreaGrp area);

	Page<AreaGrp> getAreaGrpPageByName(Page page, String areaGrpName);

	Page<AreaGrp> getFirstLevelAreaGrp(Page page);

	Set<AreaGrpModel> getAllAreaGrp(HttpServletRequest request);

	Set<Integer> getParentAreaGrpIds(Integer subAreaGrpId);

	Set<Integer> getSubAreaGrpIds(Integer rootId);


}
