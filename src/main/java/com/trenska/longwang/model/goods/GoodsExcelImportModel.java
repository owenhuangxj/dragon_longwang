package com.trenska.longwang.model.goods;

import com.alibaba.excel.annotation.ExcelProperty;
import com.trenska.longwang.entity.goods.GoodsSpec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Owen
 * @since 2019-08-01
 */
@Data
@ApiModel("商品Excel导入模型")
@NoArgsConstructor
public class GoodsExcelImportModel {

	@ExcelProperty(index = 0)
	@ApiModelProperty("编号")
	private String goodsNo;

	@ExcelProperty(index = 1)
	@ApiModelProperty("条码")
	private String barcode;

	@ExcelProperty(index = 2)
	@ApiModelProperty("名称")
	private String goodsName;

	@ExcelProperty(index = 3)
	@ApiModelProperty("单位")
	private String mainUnit;

	@ExcelProperty(index = 4)
	@ApiModelProperty("品牌")
	private String brandName;

	@ExcelProperty(index = 5)
	@ApiModelProperty("一级分类")
	private String frtCatName;

	@ExcelProperty(index = 6)
	@ApiModelProperty("二级分类")
	private String scdCatName;

	@ExcelProperty(index = 7)
	@ApiModelProperty("上架/下架")
	private String status;

	@ExcelProperty(index = 8)
	@ApiModelProperty("起订量")
	private Integer minOdrQtt;

	@ExcelProperty(index = 9)
	@ApiModelProperty("初始库存")
	private Integer initStock;

	@ExcelProperty(index = 10)
	@ApiModelProperty("定价")
	private String price;

	@ExcelProperty(index = 11)
	@ApiModelProperty("过期时间")
	private Integer expire;

	@ExcelProperty(index = 12)
	@ApiModelProperty("备注")
	private String remarks;

}