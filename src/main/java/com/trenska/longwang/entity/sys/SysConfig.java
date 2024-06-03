package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 2019/4/11
 * 创建人:Owen
 */
@Data
@ApiModel("系统配置类")
@TableName("t_sys_config")
@NoArgsConstructor
/**
 * 当报mybatis-plus entity XXX Not Found TableInfoCache.错误时，则缺少实体类对应的Mapper，添加mapper即可
 */
public class SysConfig extends Model<SysConfig> {
	@TableId(type = IdType.INPUT)
	private Integer sysEmpId = 0;
	@ApiModelProperty("显示商品图片开关 true : 显示 false 不显示")
	private Boolean goodsImgable = true;
	@ApiModelProperty("小数保留位数 默认为 2")
	private Integer retain = 2;
	@ApiModelProperty("出库方式 : true 按照批次出库 false : 不按照批次出库")
	private Boolean stockoutByMadedate;

	public SysConfig(Integer sysEmpId){
		this.sysEmpId = sysEmpId;
	}
	//重写这个方法，return当前类的主键
	@Override
	protected Serializable pkVal() {
		return sysEmpId;
	}
}
