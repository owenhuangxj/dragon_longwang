package com.trenska.longwang.model.customer;

import lombok.Data;

/**
 * 2019/6/30
 * 创建人:Owen
 */
@Data
public class CustomerExportModel {
	private String custNo;

	private String custName;

	private String priceGrp;

	private String areaGrp;

	private String empName;

	private String custType;

	private String debtLimit;

	private String linkman;

	private String linkPhone;

	private String addr;
}