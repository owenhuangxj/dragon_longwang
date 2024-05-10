package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 创建人:Owen
 * 2021-09-04
 */
@Data
@ApiModel("客户销售汇总查询Model")
@NoArgsConstructor
public class CustSalesSummarizingSearchModel {
	@ApiModelProperty(name = "remarks", value = "商品备注", dataType = "string")
	String remarks;
	@ApiModelProperty(name = "custId", value = "客户id", dataType = "int")
	Integer custId;
	@ApiModelProperty(name = "empId", value = "员工id", dataType = "int")
	Integer empId;
	@ApiModelProperty(name = "employeeId", value = "员工id,数据权限用", dataType = "int")
	Integer employeeId;
	@ApiModelProperty(name = "custName", value = "客户名称", dataType = "string")
	String custName;
	@ApiModelProperty(name = "endTime", value = "时间段-结束", dataType = "LocalDateTime")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss",iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime endTime;
	@ApiModelProperty(name = "brandId", value = "品牌id", dataType = "int")
	Integer brandId;
	@ApiModelProperty(name = "brandName", value = "品牌名称", dataType = "string")
	String brandName;
	@ApiModelProperty(name = "beginTime", value = "时间段-开始", dataType = "LocalDateTime")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss",iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime beginTime;
	@ApiModelProperty(name = "frtCatName", value = "一级分类名称", dataType = "string")
	String frtCatName;
	@ApiModelProperty(name = "scdCatName", value = "二级分类名称", dataType = "string")
	String scdCatName;
	@ApiModelProperty(name = "shipman", value = "送货人名称", dataType = "string")
	String shipman;
	@ApiModelProperty(name = "shipmanId", value = "送货人id", dataType = "int")
	Integer shipmanId;
	@ApiModelProperty(name = "salesman", value = "业务员名称", dataType = "string")
	String salesman;
	@ApiModelProperty(name = "salesmanId", value = "业务员id", dataType = "int")
	Integer salesmanId;
	@ApiModelProperty(name = "areaGrpId", value = "区域id", dataType = "int")
	Integer areaGrpId;
	@ApiModelProperty(name = "areaGrpIds", value = "区域id集合", dataType = "int" ,hidden = true)
	List<Integer> areaGrpIds;
	@ApiModelProperty(name = "goodsScope", value = "正品/赠品/所有", dataType = "int")
	Integer goodsScope;
	@ApiModelProperty(name = "areaGrpName", value = "区域名称", dataType = "string")
	String areaGrpName;
}