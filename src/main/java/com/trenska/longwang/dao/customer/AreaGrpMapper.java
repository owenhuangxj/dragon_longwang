package com.trenska.longwang.dao.customer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.entity.goods.Active;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface AreaGrpMapper extends BaseMapper<AreaGrp> {

	List<Integer> selectAllChildrenByAreaGrpId(int areaGrpId);

	List<AreaGrp> selectAreaGrpPage(Pagination pagination);

	List<AreaGrp> selectThirdClassAreaGrpPage(Pagination pagination);

	List<AreaGrp> selectAreaGrpsByEmpId(int empId);

	List<AreaGrp> selectSubAreaGrpSelective(Integer areaId);

	@Select("select ifnull(max(area_grp_id) + 1 , 0) from t_area_grp where pid = #{pid}")
	Integer getMaxAreaGrpIdByPid(Integer pid);

	@Select("select * from t_area_grp where area_grp_name like concat( '%', #{areaGrpName},'%')")
	List<AreaGrp> selectAreaGrpPageByName(Pagination page, String areaGrpName);

	/**
	 * 获取pid为 @Param pid 的所有区域信息
	 * @param page 页面基本信息
	 * @param pid 父区域id
	 * @return
	 */
	List<AreaGrp> selectFirstLevelAreaGrpPage(Pagination page, @NotNull Integer pid);

	/**
	 * 查询客户所参加的活动
	 * 	1.涉及的表
	 * 		t_active
	 * 		t_active_area_grp
	 *
	 * @param custId
	 * @return
	 */
	Set<Active> selectCustAreaGrpActiveInfo(@Param("custId") Integer custId , @Param("goodsId") Integer goodsId);

	/**
	 * 根据下级areaGrpId 获取树中从下级区域分组到第一级区域分组的id
	 * @param subAreaGrpId
	 * @return
	 */
	Set<Integer> selectParentAreaGrpIds(Integer subAreaGrpId);

	Set<Integer> selectSubAreaGrpIds(Integer rootId);

	@Select("select area_grp_id from t_area_grp where pid = #{rootId}")
	List<Integer> selectDirectSubAreaGrpIds(Integer rootId);

	@Select("select area_grp_name from t_area_grp where area_grp_id = #{areaGrpId}")
	String selectAreaGrpNameByAreaGrpId(Integer areaGrpId);
}
