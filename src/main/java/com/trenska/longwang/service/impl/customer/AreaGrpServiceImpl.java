package com.trenska.longwang.service.impl.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.sys.EmpAreaGrpMapper;
import com.trenska.longwang.dao.sys.SysEmpMapper;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.sys.EmpAreaGrp;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.customer.AreaGrpModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.IAreaGrpService;
import com.trenska.longwang.service.sys.IEmpAreaGrpService;
import com.trenska.longwang.util.ObjectCopier;
import com.trenska.longwang.util.SysUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@Service
@SuppressWarnings("all")
public class AreaGrpServiceImpl extends ServiceImpl<AreaGrpMapper, AreaGrp> implements IAreaGrpService {

	@Autowired
	private SysEmpMapper empMapper;

	@Autowired
	private EmpAreaGrpMapper empAreaGrpMapper;

	@Autowired
	private IEmpAreaGrpService empAreaGrpService;

	@Autowired
	private CustomerMapper customerMapper;

	@Override
	public List<AreaGrp> getSubAreaGrp(Integer areaGrpId) {
		return super.baseMapper.selectSubAreaGrpSelective(areaGrpId);
	}

	/**
	 * com.baomidou.mybatisplus.plugins.Page 将传入的参数进行了处理，
	 * 会根据传入记录总数，每页显示的记录数量算出总页数等信息，此处直接使用，也可以自己封装
	 */
	@Override
	public Page<AreaGrp> getAreaGrpPage(Page page) {
		page.setRecords(super.baseMapper.selectAreaGrpPage(page));
		page.setTotal(this.count());
		return page;
	}

	@Override
	public Page<AreaGrp> getThirdClassAreaGrpPage(Page page) {
		page.setRecords(super.baseMapper.selectThirdClassAreaGrpPage(page));
		page.setTotal(super.baseMapper.selectCount(new QueryWrapper<AreaGrp>().eq("area_grp_deep", 3)));
		return page;
	}

	@Override
	@Transactional
	public ResponseModel addSubAreaGrp(AreaGrp areaGrp) {

		AreaGrp oldAreaGrp = this.getOne(
				new LambdaQueryWrapper<AreaGrp>()
						.eq(AreaGrp::getAreaGrpName, areaGrp.getAreaGrpName())
		);

		if (null != oldAreaGrp) {
			return ResponseModel.getInstance().succ(false).msg("区域分组名称已经存在，不能创建");
		}

		// 查询父节点id => pid 查询该父节点下最大的子节点的id => area_grp_id并加上 1 作为下一个子节点的area_grp_id
		Integer maxAreaGrpId = super.baseMapper.getMaxAreaGrpIdByPid(areaGrp.getPid());
		// 如果获取的子节点的id为0，则子节点值area_grp_id为父节点的值*100
		if (!BooleanUtils.toBoolean(maxAreaGrpId)) {
			// 如果t_area_grp表什么都没有，即第一次存一级区域时获取到的最大区域分组id为0，则初始一级区域分组的id即area_grp_id为10
			if (!BooleanUtils.toBoolean(areaGrp.getPid()))
				maxAreaGrpId = 10;
			else
				maxAreaGrpId = areaGrp.getPid() * 100;
		}
		areaGrp.setAreaGrpId(maxAreaGrpId);
		this.save(areaGrp);
		/**
		 * 解决拥有全部数据权限的员工对新建的区域没有权限的bug
		 */
		//////////////////////////////////////////////// 处理账号数据权限 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// 获取区域分组id
		int areaGrpId = areaGrp.getAreaGrpId();
		// 查找sys_emp表中所有all_data为1的员工
		List<SysEmp> sysEmps = empMapper.selectList(
				new LambdaQueryWrapper<SysEmp>()
						.eq(SysEmp::getAllData, true)
						.select(SysEmp::getEmpId)
		);

		List<Integer> empIds = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(sysEmps)){
			empIds.addAll(sysEmps.stream().map(SysEmp::getEmpId).collect(Collectors.toList()));
			List<EmpAreaGrp> empAreaGrps = new ArrayList<>();
			empIds.forEach(empId->{
				empAreaGrps.add(new EmpAreaGrp(empId,areaGrpId));
			});
			if(!empAreaGrps.isEmpty()){
				empAreaGrpService.saveBatch(empAreaGrps);
//				empAreaGrpService.saveOrUpdateBatch(empAreaGrps);
			}
		}
		//////////////////////////////////////////////// 处理账号数据权限结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		return ResponseModel.getInstance().succ(true).msg("区域分组添加成功");

	}

	/**
	 * 客户在此区域怎么处理==>不能删除
	 * @param areaId
	 * @param areaDeep
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel removeAreaGrp(Integer areaId, Integer areaDeep) {

		Set<Integer> subAreaGrpIds = super.baseMapper.selectSubAreaGrpIds(areaId);
		/************************************如果有客户在该区域，该区域就不能删除************************************/
		List<Customer> dbCustomers = customerMapper.selectList(
				new LambdaQueryWrapper<Customer>()
						.in(Customer::getAreaGrpId,subAreaGrpIds)
		);

		if(Objects.isNull(dbCustomers) && !dbCustomers.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("有"+dbCustomers.size()+"属于该区域，不能删除该区域");
		}

		switch (areaDeep) {
			// 区域深度为1，表示是大区，需要在删除大区的时候同时删除所有小区
			case 1:
				List<AreaGrp> cities = super.baseMapper.selectList(new QueryWrapper<AreaGrp>().eq("pid", areaId));
				List<Integer> cityIds = new ArrayList<>();
				// 删除二三级CustArea
				cities.forEach(city -> {
					cityIds.add(city.getAreaGrpId());
					// 根据二级areaId删除三级CustArea
					super.baseMapper.delete(new QueryWrapper<AreaGrp>().eq("pid", city.getAreaGrpId()));
					// 删除二级CustArea
					super.baseMapper.delete(new QueryWrapper<AreaGrp>().eq("area_grp_id", city.getAreaGrpId()));
				});
				this.removeById(areaId);
				break;
			case 2:
				super.baseMapper.delete(new QueryWrapper<AreaGrp>().eq("pid", areaId));
				break;
		}
		/*******************************************删除t_emp_area_grp关联数据*******************************************/
		empAreaGrpMapper.delete(
				new LambdaQueryWrapper<EmpAreaGrp>()
						.in(EmpAreaGrp::getAreaGrpId,subAreaGrpIds)
		);

		return ResponseModel.getInstance().succ(true).msg("区域分组删除成功");
	}

	@Override
	@Transactional
	public Boolean updateAreaGrp(AreaGrp area) {
		return BooleanUtils.toBoolean(
				super.baseMapper.update(area, new UpdateWrapper<AreaGrp>()
						.eq("area_grp_id", area.getAreaGrpId())
				)
		);
	}

	@Override
	public Page<AreaGrp> getAreaGrpPageByName(Page page, String areaGrpName) {
		List<AreaGrp> records = super.baseMapper.selectAreaGrpPageByName(page, areaGrpName);
		page.setRecords(records);
		page.setTotal(count(new QueryWrapper<AreaGrp>().like("area_grp_name", areaGrpName)));
		return page;
	}

	@Override
	public Page<AreaGrp> getFirstLevelAreaGrp(Page page) {
		List<AreaGrp> records = super.baseMapper.selectFirstLevelAreaGrpPage(page, 0);
		page.setRecords(records);
		page.setTotal(count(new QueryWrapper<AreaGrp>().eq("pid", 0)));
		return page;
	}

	public Set<Integer> getParentAreaGrpIds(Integer subAreaGrpId){
		return this.baseMapper.selectParentAreaGrpIds(subAreaGrpId);
	}

	public Set<Integer> getSubAreaGrpIds(Integer rootId){
		return this.baseMapper.selectSubAreaGrpIds(rootId);
	}


	/**
	 * 获取所有客户分组信息
	 * @return
	 */
	@Override
	public Set<AreaGrpModel> getAllAreaGrp(HttpServletRequest request) {
		Integer empIdInToken = SysUtil.getEmpIdInToken();

		List<AreaGrp> areaGrpList = this.list();

		Set<AreaGrp> areaGrps = areaGrpList.stream().collect(Collectors.toSet());

		Set<AreaGrpModel> areaGrpModels = new HashSet<>();

		areaGrps.forEach(areaGrp -> {
			AreaGrpModel areaGrpModel = new AreaGrpModel();
			ObjectCopier.copyProperties(areaGrp,areaGrpModel);
			areaGrpModels.add(areaGrpModel);
		});

		areaGrpModels.forEach(areaGrpModel -> {
			Set<AreaGrpModel> subAreaGrps = getSubAreaGrps(areaGrpModel.getAreaGrpId(), areaGrpModels);
			areaGrpModel.setSubAreaGrps(subAreaGrps);
		});

		// 因为是循环嵌套，只返回一级区域
		return areaGrpModels.stream().filter(areaGrpModel ->
				areaGrpModel.getPid() == 0
		).collect(Collectors.toSet());

	}

	/**
	 * 递归处理子区域分组
	 * @param areaGrpId
	 * @param areaGrpSource
	 * @return
	 */
	private Set<AreaGrpModel> getSubAreaGrps(Integer areaGrpId ,Set<AreaGrpModel> areaGrpSource) {

		Set<AreaGrpModel> subAreaGrps = new HashSet<>();

		// 获取区域分组的直接子区域分组
		areaGrpSource.forEach(areaGrpModel -> {
			/*必须是equals方法*/
			if(areaGrpModel.getPid().equals(areaGrpId)){
				subAreaGrps.add(areaGrpModel);
			}
		});

		//递归调用来处理子区域分组
		for(AreaGrpModel areaGrpModel : subAreaGrps){
			areaGrpModel.setSubAreaGrps(getSubAreaGrps(areaGrpModel.getAreaGrpId(),areaGrpSource));
		}

		// 如果子区域为空返回空集合而不是null
		if(CollectionUtils.isEmpty(subAreaGrps)){
			return new HashSet<>();
		}

		return subAreaGrps;

	}


}