package com.trenska.longwang.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@Data
@TableName("t_area_grp")
@ApiModel("客户区域分组实体")
public class AreaGrp extends Model<AreaGrp> {

	@ApiModelProperty("区域分组id")
	@TableId(type = IdType.INPUT)
	private Integer areaGrpId;

	@ApiModelProperty("区域分组名称")
	private String areaGrpName;

	@ApiModelProperty("区域分组的父节点id，即父节点的areaGrpId")
	private Integer pid;

	@Min(1)
	@Max(3)
	@ApiModelProperty("区域深度，一共三级，分别对应1、2、3. \n1：一级区域；2：二级区域；3：三级区域")
	private Integer areaGrpDeep;

	public AreaGrp(int areaGrpId){
		this.areaGrpId = areaGrpId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AreaGrp)) return false;
		AreaGrp areaGrp = (AreaGrp) o;
		return areaGrpId.equals(areaGrp.areaGrpId) &&
				areaGrpName.equals(areaGrp.areaGrpName) &&
				pid.equals(areaGrp.pid) &&
				areaGrpDeep.equals(areaGrp.areaGrpDeep);
	}

	@Override
	public int hashCode() {
		return Objects.hash(areaGrpId, areaGrpName, pid, areaGrpDeep);
	}
}
