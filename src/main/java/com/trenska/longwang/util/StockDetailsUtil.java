package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.constant.WarningLevel;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.sys.ResponseModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 2019/8/7
 * 创建人:Owen
 */
public class StockDetailsUtil {

	public static ResponseModel saveStockDetails(StockDetail stockDetail) {
		StockDetails stockDetails = new StockDetails();
		ObjectCopier.copyProperties(stockDetail, stockDetails);
		Unit unit = new Unit();
		Integer unitId = stockDetail.getUnitId();
		Unit dbUnit = unit.selectOne(
				new LambdaQueryWrapper<Unit>()
						.eq(Unit::getUnitId,unitId)
						.or()
						.eq(Unit::getDeleted,1)
						.eq(Unit::getUnitId,unitId)
		);
		stockDetails.setUnitName(dbUnit.getUnitName());
		String prefix = "+";
		String stockType = stockDetail.getStockType();
		if (Constant.RKDZF_CHINESE.equals(stockType) || Constant.CKD_CHINESE.equals(stockType)) {
			prefix = "-";
		}
		String history = prefix + stockDetail.getHistory();
		stockDetails.setHistory(history);
		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		stockDetails.setStockTime(currentTime);
		stockDetails.insert();

		return ResponseModel.getInstance().succ(true).msg("Ok");
	}

	/**
	 * @param passedMonths 已过时间(月)
	 * @return
	 */
	public static String getWarningLevel(int passedMonths ){
		if(passedMonths <= 2){
			return WarningLevel.NEW;
		}else if(passedMonths > 2 && passedMonths <= 3){
			return WarningLevel.UNSALABLING;
		}else if(passedMonths > 3 && passedMonths <= 10){
			return WarningLevel.UNSALABLE;
		}else if(passedMonths > 10 && passedMonths <= 12){
			return WarningLevel.EXPIRING;
		}else if(passedMonths > 12){
			return WarningLevel.EXPIRED;
		}else{
			return WarningLevel.INIT_STOCK;
		}
	}

	public static String getWarningLevel(int passedDays , int expire){
			if (passedDays < expire / 12) { //新货: 剩余保质期 >=11/12
				return WarningLevel.NEW;
			} else if (passedDays > expire / 4 && passedDays <= expire / 2) {//滞销风险: 1/2 <=剩余保质期<=3/4
				return WarningLevel.UNSALABLING;
			} else if (passedDays > expire / 2 && passedDays <= expire * 5 / 6) {
				return WarningLevel.UNSALABLE; //滞销商品:
			} else if (passedDays > expire * 5 / 6 && passedDays <= expire) {
				return WarningLevel.EXPIRING;
			} else if(passedDays >= expire) {
				return WarningLevel.EXPIRED;
			}else {
				return WarningLevel.INIT_STOCK;
			}
	}


//	if(Objects.nonNull(warning.getLeftDays())) {
//		Integer expir = warning.getExpir();
//		int passedDays = warning.getExpir() - warning.getLeftDays();
//		if (passedDays < expir / 12) { //新货: 剩余保质期 >=11/12
//			warning.setWarningLevel(WarningLevel.NEW);
//		} else if (passedDays > expir / 4 && passedDays <= expir / 2) {//滞销风险: 1/2 <=剩余保质期<=3/4
//			warning.setWarningLevel(WarningLevel.UNSALABLING);
//		} else if (passedDays > expir / 2 && passedDays <= expir * 5 / 6) {
//			warning.setWarningLevel(WarningLevel.UNSALABLE); //滞销商品:
//		} else if (passedDays > expir * 5 / 6 && passedDays <= expir) {
//			warning.setWarningLevel(WarningLevel.EXPIRING);
//		} else if (passedDays >= expir) {
//			warning.setWarningLevel(WarningLevel.EXPIRED);
//		}
//	}else{
//		warning.setWarningLevel(WarningLevel.INIT_STOCK);
//	}
}
