package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.ActiveAreaGrpMapper;
import com.trenska.longwang.dao.goods.ActiveGoodsMapper;
import com.trenska.longwang.dao.goods.ActiveMapper;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.entity.goods.Active;
import com.trenska.longwang.entity.goods.ActiveAreaGrp;
import com.trenska.longwang.entity.goods.ActiveGoods;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IActiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * 商品活动服务实现类
 *
 * @author Owen
 * @since 2019-04-12
 */
@Service
@SuppressWarnings("all")
public class ActiveServiceImpl extends ServiceImpl<ActiveMapper, Active> implements IActiveService {

	@Autowired
	private ActiveAreaGrpMapper activeAreaGrpMapper;

	@Autowired
	private ActiveGoodsMapper activeGoodsMapper;

	@Autowired
	private IndentDetailMapper indentDetailMapper;

	@Autowired
	private IndentMapper indentMapper;

	@Override
	public Page<Active> getActivePage(Page page) {
		page.setRecords(super.baseMapper.selectActivePage(page));
		page.setTotal(count());
		super.baseMapper.updateActiveStat();
		return page;
	}

	@Override
	public Page<Active> getActivePageSelective(Active active, Page page) {
		page.setRecords(super.baseMapper.selectActivePageSelective(active, page));
		page.setTotal(super.baseMapper.selectCountSelective(active));
		super.baseMapper.updateActiveStat();
		return page;
	}

	/**
	 * 所有商品都参加不需要处理，查询的时候处理
	 * @param active
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel saveActive(Active active) {
		Boolean isAllJoin = active.getIsAllJoin();
		if( (isAllJoin == null && active.getActiveGoods().isEmpty()) || (isAllJoin != null && isAllJoin ==false && active.getActiveGoods().isEmpty())){
			return ResponseModel.getInstance().succ(false).msg("请选择活动商品");
		}
		// 首先保存活动数据到t_active
		this.save(active);
		// 获取保存的活动id
		Integer activeId = active.getActiveId();

		Set<ActiveGoods> activeGoods = active.getActiveGoods();
		// 保存参加促销活动的商品促销详情
		for (ActiveGoods activeGds : activeGoods) {
			activeGds.setActiveId(activeId);
			activeGoodsMapper.insert(activeGds);
		}

		Set<Integer> areaGrpIds = active.getAreaGrpIds();
		// 保存数据到t_active_area_grp
		for (Integer areaGrpId : areaGrpIds) {
			activeAreaGrpMapper.insert(new ActiveAreaGrp(activeId, areaGrpId));
		}
		return ResponseModel.getInstance().succ(true).msg("创建商品活动成功");

	}

	@Override
	@Transactional
	public ResponseModel removeActiveById(Integer activeId) {

		Active active = this.getById(activeId);

		if (null == active){
			return ResponseModel.getInstance().succ(false).msg("无效的活动");
		}

		if(active.getStat()){
			return ResponseModel.getInstance().succ(false).msg("不能删除未结束的活动");
		}

		includeUnfinishedIndents(new ArrayList<Integer>(){{
				this.add(activeId);
		}});

		this.removeById(activeId);
		activeGoodsMapper.delete(new QueryWrapper<ActiveGoods>().eq("active_id", activeId));
		activeAreaGrpMapper.delete(new QueryWrapper<ActiveAreaGrp>().eq("active_id", activeId));
		return ResponseModel.getInstance().succ(true).msg("删除商品活动成功");
	}

	@Override
	public ResponseModel removeActiveByIds(Collection<Integer> activeIds) {

		int count = this.count(
				new LambdaQueryWrapper<Active>()
						.in(Active::getActiveId,activeIds)
						.eq(Active::getStat,true)
		);

		if(activeIds.size() != count){
			return ResponseModel.getInstance().succ(false).msg("你选择了未结束的活动或活动中包含无效的活动");
		}

		includeUnfinishedIndents(activeIds);

		this.removeByIds(activeIds);
		activeGoodsMapper.delete(new QueryWrapper<ActiveGoods>().in("active_id", activeIds));
		activeAreaGrpMapper.delete(new QueryWrapper<ActiveAreaGrp>().in("active_id", activeIds));
		return ResponseModel.getInstance().succ(true).msg("删除商品活动成功");
	}

	@Override
	public Active getInfoById(Integer activeId) {
		return super.baseMapper.selectActiveById(activeId);
	}

	private ResponseModel includeUnfinishedIndents(Collection<Integer> activeIds){
		// 判断是否有未完成的订单参加了活动
		boolean hasUnfinishedIndents = indentMapper.hasUnfinishedIndents(activeIds);
		if (hasUnfinishedIndents) {
			return ResponseModel.getInstance().succ(false).msg("删除活动失败 : 还有参加了活动但未完成的订单");
		}else {
			return ResponseModel.getInstance().succ(true).msg("OK");
		}
	}
}
