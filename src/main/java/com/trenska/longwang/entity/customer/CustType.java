package com.trenska.longwang.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 2019/4/5
 * 创建人:Owen
 * 客户类型实体类
 */
@Data
@ApiModel("客户类型实体类")
@TableName("t_cust_type")
public class CustType {
	@ApiModelProperty("类型id")
	@TableId(type = IdType.AUTO)
	private Integer custTypeId;
	@ApiModelProperty("类型名")
	@NotNull
	private String custTypeName;
	@ApiModelProperty("类型值")
	private Integer custTypeVal;

}
