package com.trenska.longwang.model.stock;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 2019/4/17
 * 创建人:Owen
 */
@Data
@ApiModel("用于向前端传递查询结果的库存Model")
public class StockDetailModel {
	private Long stockinId;
	private String busiNo;
	private LocalDateTime stockTime;
	private String stockType;
	private String operType;
	private Integer num;
	private String unitName;
	private Integer stock;
	private LocalDate madeDate;
	private Boolean stat;
}
