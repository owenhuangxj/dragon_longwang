package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.constant.WarningLevel;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.model.sys.ResponseModel;

/**
 * 2019/8/7
 * 创建人:Owen
 */
public class StockDetailsUtil {

	/**
	 * 记录一条库存明细记录到仓库日志明细表
	 *
	 * @param stockDetail 库存变更信息
	 * @return 成功返回 true,OK
	 */
	public static ResponseModel dbLogStockDetail(StockDetail stockDetail) {
		StockDetails stockDetails = new StockDetails();
		ObjectCopier.copyProperties(stockDetail, stockDetails);
		Integer unitId = stockDetail.getUnitId();
		Unit dbUnit = new Unit().selectOne(
				new LambdaQueryWrapper<Unit>()
						.eq(Unit::getUnitId, unitId)
						.or()
						.eq(Unit::getDeleted, 1)
						.eq(Unit::getUnitId, unitId)
		);
		stockDetails.setUnitName(dbUnit.getUnitName());
		String prefix = DragonConstant.PLUS;
		String stockType = stockDetail.getStockType();
		if (DragonConstant.RKDZF_CHINESE.equals(stockType) || DragonConstant.CKD_CHINESE.equals(stockType)) {
			prefix = DragonConstant.MINUS;
		}
		String history = prefix + stockDetail.getHistory();
		stockDetails.setHistory(history);
		String currentTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		stockDetails.setStockTime(currentTime);
		stockDetails.setTimestamp(System.currentTimeMillis());
		stockDetails.insert();

		return ResponseModel.getInstance().succ(true).msg("Ok");
	}

	/**
	 * @param passedMonths 已过时间(月)
	 * @return
	 */
	public static String getWarningLevel(int passedMonths) {
		if (passedMonths <= 2) {
			return WarningLevel.NEW;
		} else if (passedMonths > 2 && passedMonths <= 3) {
			return WarningLevel.UNSALABLING;
		} else if (passedMonths > 3 && passedMonths <= 10) {
			return WarningLevel.UNSALABLE;
		} else if (passedMonths > 10 && passedMonths <= 12) {
			return WarningLevel.EXPIRING;
		} else if (passedMonths > 12) {
			return WarningLevel.EXPIRED;
		} else {
			return WarningLevel.INIT_STOCK;
		}
	}
}