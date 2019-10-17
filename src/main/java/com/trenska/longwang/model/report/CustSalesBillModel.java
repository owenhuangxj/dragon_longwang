package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 2019/5/30
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售总账")
@NoArgsConstructor
public class CustSalesBillModel {
	private CustSalesSummationModel summation;
	List<CustSalesBillRecordsModel> records = new ArrayList<>();
}
