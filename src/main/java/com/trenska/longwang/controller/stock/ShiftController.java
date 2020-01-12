package com.trenska.longwang.controller.stock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.stock.Shift;
import com.trenska.longwang.model.sys.ExistModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.stock.IShiftService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Owen
 * @since 2019-07-04
 */
@Slf4j
@RestController
@RequestMapping("/shift")
@Api(description = "库存班次接口")
public class ShiftController {

	@Autowired
	private IShiftService shiftService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "shiftName", value = "库存班次名称", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("添加班次")
	public ResponseModel addBrand(@RequestBody Shift shift) {
		if(Objects.isNull(shift)){
			return ResponseModel.getInstance().succ(false).msg("无效的班次");
		}
		String shiftName = shift.getShiftName();

		if(StringUtils.isEmpty(shiftName)){
			return ResponseModel.getInstance().succ(false).msg("无效的班次");
		}

		ExistModel existModel = checkShift(shiftName);
		if(existModel.isExists()){
			return ResponseModel.getInstance().succ(false).msg("班次已经存在");
		}
		shiftService.save(shift);
		return ResponseModel.getInstance().succ(true).msg("班次添加成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{shiftId}")
	@ApiOperation("删除库存班次")
	public ResponseModel deleteBrand(@ApiParam(name = "shiftId", required = true) @PathVariable("shiftId") int shiftId) {
		if(shiftId < 0){
			return ResponseModel.getInstance().succ(false).msg("无此库存班次");
		}
		Shift dbShift = shiftService.getById(shiftId);
		if(Objects.isNull(dbShift)){
			return ResponseModel.getInstance().succ(false).msg("无此库存班次");
		}
		shiftService.removeById(shiftId);
		return ResponseModel.getInstance().succ(true).msg("库存班次删除成功");
	}
	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除库存班次")
	public ResponseModel batchDeleteBrand(
			@ApiParam(name = "shiftIds", value = "需要批量删除的库存班次id集合/数组", required = true) @RequestParam(value = "shiftIds") Collection<Integer> shiftIds) {
		if (shiftIds.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("无效的班次信息");
		}

		Collection<Shift> shifts = shiftService.listByIds(shiftIds);
		if (Objects.isNull(shifts) || shifts.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("无效的班次信息");
		}

		shiftService.removeByIds(shiftIds);
		return ResponseModel.getInstance().succ(true).msg("批量删除成功");
	}

	@PutMapping("/update")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "shiftId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "shiftName", paramType = "body", dataType = "string")
	})
	@ApiOperation("修改批次名称")
	public ResponseModel updateBrand(@RequestBody Shift shift) {
		if(Objects.isNull(shift)){
			return ResponseModel.getInstance().succ(false).msg("无效的批次");
		}
		if(NumberUtil.isIntegerNotUsable(shift.getShiftId())){
			return ResponseModel.getInstance().succ(false).msg("无效的批次");
		}
		if(StringUtils.isEmpty(shift.getShiftName())){
			return ResponseModel.getInstance().succ(false).msg("批次名称不能为空");
		}
		shiftService.updateById(shift);
		return ResponseModel.getInstance().succ(true).msg("批次更新成功");
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("分页库存班次")
	public PageHelper<Shift> listBrandPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Shift> pageInfo = shiftService.getShiftPage(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/exists/{shiftName}")
	@ApiOperation("查询班次是否存在")
	public ExistModel checkShift(@PathVariable("shiftName") String shiftName) {
		Shift shift = shiftService.getOne(
				new LambdaQueryWrapper<Shift>()
						.eq(Shift::getShiftName, shiftName)
		);
		String msg = "班次已存在";

		ExistModel existModel = new ExistModel();
		if(Objects.isNull(shift)){
			msg = "班次可用";
			existModel.setExists(false);
		}
		existModel.setMsg(msg);
		return existModel;
	}
}