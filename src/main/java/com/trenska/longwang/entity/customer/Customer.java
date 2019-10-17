package com.trenska.longwang.entity.customer;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trenska.longwang.entity.sys.SysEmp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 2019/4/3
 * 创建人:Owen
 * 为了便于使用mybatis-plus和前后端数据传递以及尽可能的减少类的创建(便于后期维护)
 * 创建了实体类属性(SysEmp emp = new SysEmp();)和与数据库表字段关联的属性(Integer empId)
 * 两者并不矛盾：一个(emp)用于接受数据库返回的对象数据，
 * 一个(empId)用于接受前端传递的数据并与数据库字段关联以便使用mybatis-plus提供的方法进行CRUD
 * 其它类有相同的操作不再赘述
 */
@Data
@ApiModel("客户实体")
@TableName("t_customer")
@JsonIgnoreProperties(value = {"handler"})
@NoArgsConstructor
public class Customer extends Model<Customer> {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("客户id，新建时由系统生成，修改时必须传递该属性")
	private Integer custId;

	@NotNull(message = "客户编号不能为空")
	@ApiModelProperty(value = "客户编号,新建时必须传递该属性",required = true)
	private String custNo;

	@NotNull(message = "客户名称不能为空")
	@ApiModelProperty(value = "客户名称，新建时必须传递该属性",required = true)
	private String custName;

	@ApiModelProperty(value = "查询时接受会员价格分组实体类数据，传参时忽略",readOnly = true)
	@TableField(exist = false)
	private PriceGrp priceGrp;

	@ApiModelProperty(value = "查询时接受客户区域分组实体类数据，传参时忽略",readOnly = true)
	@TableField(exist = false)
	private AreaGrp areaGrp;

	@ApiModelProperty(value = "查询时接受客户类型实体类数据，传参时忽略",readOnly = true)
	@TableField(exist = false)
	private CustType custType;

	@TableField(exist = false)
	@ApiModelProperty(value = "查询时接受负责该客户的业务员实体类数据，传参时忽略",readOnly = true)
	private SysEmp emp;

	@ApiModelProperty("会员价格分组id")
	private Integer priceGrpId;

	@ApiModelProperty(value = "客户区域分组id",required = true)
	private Integer areaGrpId;

	@ApiModelProperty("客户类型id")
	private Integer custTypeId;

//	@Pattern(regexp = "\\d+",message = "请输入正确的业务员信息")
	@ApiModelProperty("负责该客户的业务员id")
	private Integer  empId;

	@ApiModelProperty("客户联系人")
	private String linkman;

	@ApiModelProperty("客户信息创建时间")
	private String createdTime;

	@ApiModelProperty("客户信息修改时间")
	private String updatedTime;

	@ApiModelProperty("客户联系电话")
	private String linkPhone;

	@ApiModelProperty("客户期初欠款")
	private String initDebt;

	@ApiModelProperty("客户欠款")
	private String debt;

	@ApiModelProperty(value = "客户欠款额度",required = true)
	//@Pattern(regexp = "\\d+",message = "欠款额度必须为数字")
	private String debtLimit;

	@ApiModelProperty("客户邮箱")
	private String email;

	@ApiModelProperty("客户地址省级部分")
	private String province;

	@ApiModelProperty("客户地址市级部分")
	private String city;

	@ApiModelProperty("客户地址县级部分")
	private String county;

	@ApiModelProperty("客户地址具体街道门牌号")
	private String addr;

	@ApiModelProperty("开户行")
	private String depositBank;

	@ApiModelProperty("开户账号")
	private String bankAcct;

	@ApiModelProperty("客户税号")
	private String taxNum;

	@ApiModelProperty("客户备注")
	private String remarks;

//	@TableLogic
	@ApiModelProperty(readOnly = true,hidden = true,notes = "逻辑删除标志")
	private Boolean deleted;

	public Customer(Integer custId){
		this.custId = custId;
	}

	public Customer(Integer custId,String debt){
		this.custId = custId;
		this.debt = debt;
	}

}