/*
package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.mongodb.client.result.DeleteResult;
import com.trenska.longwang.dao.goods.ActiveAreaGrpMapper;
import com.trenska.longwang.dao.goods.ActiveGoodsMapper;
import com.trenska.longwang.dao.goods.ActiveMapper;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.entity.goods.Active;
import com.trenska.longwang.entity.goods.ActiveAreaGrp;
import com.trenska.longwang.entity.goods.ActiveGoods;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IActiveAreaGrpService;
import com.trenska.longwang.service.goods.IActiveGoodsService;
import com.trenska.longwang.service.goods.IActiveService;
import com.trenska.longwang.util.ObjectCopier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

*/
/**
 * 商品活动服务实现类
 *
 * @author Owen
 * @since 2019-04-12
 *//*

@Slf4j
@Service
@SuppressWarnings("all")
public class ActiveServiceImplDev extends ServiceImpl<ActiveMapper, Active> implements IActiveService {

	@Autowired
	private ActiveAreaGrpMapper activeAreaGrpMapper;
	@Autowired
	private IActiveAreaGrpService activeAreaGrpService;

	@Autowired
	private ActiveGoodsMapper activeGoodsMapper;
	@Autowired
	private IActiveGoodsService activeGoodsService;

	@Autowired
	private IndentDetailMapper indentDetailMapper;

	@Autowired
	private IndentMapper indentMapper;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Page<Active> getActivePage(Page page) {
		page.setRecords(super.baseMapper.selectActivePage(page));
		page.setTotal(count());
		super.baseMapper.invalidateActives();
		activeGoodsMapper.deleteInvalidateActives();
		activeAreaGrpMapper.deleteInvalidateActives();
		return page;
	}

	@Override
	public Page<Active> getActivePageSelective(Active active, Page page) {
		super.baseMapper.invalidateActives();
		activeGoodsMapper.deleteInvalidateActives();
		activeAreaGrpMapper.deleteInvalidateActives();
		page.setRecords(super.baseMapper.selectActivePageSelective(active, page));
		page.setTotal(super.baseMapper.selectCountSelective(active));
		if (active != null){
			String activeName = active.getActiveName();
			Boolean stat = active.getStat();
			String beginTime = active.getBeginTime();
			String endTime = active.getEndTime();
			Query query = new Query();
			if (StringUtils.isNotEmpty(activeName)){
				query.query(Criteria.where("active_name").is(activeName));
			}
			if (stat != null){
				query.addCriteria(Criteria.where("stat").is(stat));
			}

			if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)){
				query.addCriteria(Criteria.where("begin_time").gte(beginTime));
				query.addCriteria(Criteria.where("end_time").lte(endTime));
			}
		}
		return page;
	}

	*/
/**
	 * 所有商品都参加不需要处理，查询的时候处理
	 *
	 * @param active
	 * @return
	 *//*

	@Override
	@Transactional
	public ResponseModel saveActive(Active active) {
		// 首先保存活动数据到t_active
		this.save(active);

		// 获取保存的活动id
		Integer activeId = active.getActiveId();

		Set<ActiveGoods> activeGoods = active.getActiveGoods();
		// 保存参加促销活动的商品促销详情
		for (ActiveGoods activeGds : activeGoods) {
			activeGds.setActiveId(activeId);
//			activeGoodsMapper.insert(activeGds);
		}
		if (CollectionUtils.isNotEmpty(activeGoods)){
			activeGoodsService.saveBatch(activeGoods);
		}
		Set<Integer> areaGrpIds = active.getAreaGrpIds();
		List<ActiveAreaGrp> activeAreaGrps = new ArrayList<>();
		for (Integer areaGrpId : areaGrpIds) {
			activeAreaGrps.add(new ActiveAreaGrp(activeId, areaGrpId));
//			activeAreaGrpMapper.insert(new ActiveAreaGrp(activeId, areaGrpId));
		}
		List<Integer> gooodsIds = activeGoods.stream().map(ActiveGoods::getGoodsId).collect(Collectors.toList());
		// 保存数据到t_active_area_grp
		activeAreaGrpService.saveBatch(activeAreaGrps);

		Activity activity = new Activity();
		ObjectCopier.copyProperties(active, activity);
		areaGrpIds.forEach(areaGrpId -> {
			activity.getAreaGrps().add(new AreaGrp(areaGrpId));
		});
		gooodsIds.forEach(goodsId -> {
			activity.getGoods().add(new Goods(goodsId));
		});
		activity.setStat(true);
		Activity savedActivity = mongoTemplate.save(activity, "activity");
		return ResponseModel.getInstance().succ(true).msg("创建商品活动成功.");
	}

	@Override
	@Transactional
	public ResponseModel removeActiveById(Integer activeId) {

		Active active = this.getById(activeId);

		if (null == active) {
			return ResponseModel.getInstance().succ(false).msg("无效的活动.");
		}

		if (active.getStat()) {
			return ResponseModel.getInstance().succ(false).msg("不能删除未结束的活动.");
		}

		this.removeById(activeId);
		activeGoodsMapper.delete(new QueryWrapper<ActiveGoods>().eq("active_id", activeId));
		activeAreaGrpMapper.delete(new QueryWrapper<ActiveAreaGrp>().eq("active_id", activeId));

		Query query = Query.query(Criteria.where("activeId").is(activeId));

		DeleteResult deleteResult = mongoTemplate.remove(query, Active.class);

		long deletedCount = deleteResult.getDeletedCount();

		log.info("Deleted Count From MongoDB:{}", deletedCount);

		return ResponseModel.getInstance().succ(true).msg("删除商品活动成功.");
	}

	@Override
	public ResponseModel removeActiveByIds(Collection<Integer> activeIds) {
		*/
/* 可删除活动数量 *//*

		int deletableActiveCount = this.count(
				new LambdaQueryWrapper<Active>()
						.in(Active::getActiveId, activeIds)
						.eq(Active::getStat, true)
		);

		if (activeIds.size() != deletableActiveCount) {
			return ResponseModel.getInstance().succ(false).msg("你选择了未结束的活动或活动中包含无效的活动.");
		}

		this.removeByIds(activeIds);
		activeGoodsMapper.delete(new QueryWrapper<ActiveGoods>().in("active_id", activeIds));
		activeAreaGrpMapper.delete(new QueryWrapper<ActiveAreaGrp>().in("active_id", activeIds));

		Query query = Query.query(Criteria.where("activeId").in(activeIds));
		DeleteResult deleteResult = mongoTemplate.remove(query, Activity.class);
		log.info("Deleted Count From MongoDB:{}", deleteResult.getDeletedCount());

		return ResponseModel.getInstance().succ(true).msg("删除商品活动成功.");
	}

	@Override
	public Active getInfoById(Integer activeId) {
		Query query = Query.query(Criteria.where("activeId").is(activeId));
		Activity activity = mongoTemplate.findOne(query, Activity.class);
		log.info("Get Record {} From Activity.", activity);
		return super.baseMapper.selectActiveById(activeId);
	}
}*/
