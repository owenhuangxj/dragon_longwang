package com.trenska.longwang.controller.goods;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Active;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IActiveService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 商品活动前端控制器
 * @author Owen
 * @since 2019-04-12
 */
@RestController
@RequestMapping("/active")
@Api(description = "商品活动接口")
@CrossOrigin
@Slf4j
public class ActiveController {
	@Autowired
	private IActiveService activeService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation("添加商品活动")
	public ResponseModel addActive(@Valid @RequestBody @ApiParam Active active) {
		if(null == active){
			return ResponseModel.getInstance().succ(false).msg("无效的活动信息");
		}
		return activeService.saveActive(active);
	}

	@DeleteMapping("/delete/{activeId}")
	@CheckDuplicateSubmit
	@ApiOperation("删除商品活动")
	public ResponseModel deleteActive(@ApiParam(name = "activeId", required = true) @PathVariable("activeId") Integer activeId) {
		if(!NumberUtil.isIntegerUsable(activeId)){
			return ResponseModel.getInstance().succ(false).msg("无效的活动");
		}
		/**
		 * 通过触发器级联删除了t_active_area_grp和t_active_goods表中对应active_id的级联,所以可以不使用 removeActiveById方法
		 * CREATE TRIGGER `del_active_area_grp_trigger` AFTER DELETE ON `t_active` FOR EACH ROW begin
		 * 	delete from t_active_area_grp where t_active_area_grp.active_id = old.active_id;
		 * 	delete from t_active_goods where t_active_goods.active_id = old.active_id;
		 * end; ==> 已经取消触发器
		 */
//		Boolean successful = activeService.removeById(activeId);

		return activeService.removeActiveById(activeId);

	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品活动")
	public ResponseModel batchDeleteActive(@ApiParam(name = "activeIds", value = "需要批量删除的商品活动id集合/数组", required = true) @RequestParam(value = "activeIds") Collection<Integer> activeIds) {

		if (activeIds.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("无效的活动");
		}
		return activeService.removeActiveByIds(activeIds);
	}

	@CheckDuplicateSubmit
	@PutMapping("/close/{activeId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "activeId", value = "商品活动id", paramType = "body", dataType = "int")
	})
	@ApiOperation("关闭商品活动")
	public ResponseModel downActive(@PathVariable Integer activeId) {
		Active active = new Active();
		active.setActiveId(activeId);
		active.setStat(false);
		boolean successful = activeService.updateById(active);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "关闭商品活动成功" : "关闭商品活动失败");
	}

	@CheckDuplicateSubmit
	@PutMapping("/close/batch")
	@ApiOperation("批量关闭商品活动")
	public ResponseModel batchDownActive(@Valid @RequestParam Collection<Integer> activeIds) {
		boolean successful = activeService.update(new UpdateWrapper<Active>().in("active_id",activeIds).set("stat",false));
		return ResponseModel.getInstance().succ(successful).msg(successful ? "批量关闭商品活动成功" : "批量关闭商品活动失败");
}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("普通分页")
	public PageHelper<Active> listActivePage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<Active> pageInfo = activeService.getActivePage(PageUtils.getPageParam(new PageHelper(current, size)));
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@PostMapping("/list/page/search/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "activeName", value = "商品活动名称", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "商品活动开始时间，格式为yyyy-MM-dd", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "商品活动结束时间，格式为yyyy-MM-dd", paramType = "body", dataType = "string" ),
			@ApiImplicitParam(name = "stat", value = "是否在活动中，true:活动中，false:非活动中", paramType = "body", dataType = "boolean")
	})
	@ApiOperation("条件查询分页")
	public PageHelper<Active> listActivePageSelective(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestBody Active active) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Active> pageInfo = activeService.getActivePageSelective(active, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/info/{activeId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "activeId", value = "商品活动id", paramType = "body", dataType = "int")
	})
	@ApiOperation("商品活动详情")
	public Active getActiveInfo(@PathVariable Integer activeId) {
		return activeService.getInfoById(activeId);
	}
}