package com.trenska.longwang.model.sys;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 2019/5/18
 * 创建人:Owen
 */
@Data
@ApiModel("权限模型")
@NoArgsConstructor
public class PermModel {
	@ApiModelProperty("权限值")
	private String pval;

	@ApiModelProperty("权限名")
	private String pname;

	@ApiModelProperty("权限类型")
	private Integer ptype;

	@ApiModelProperty("权限父节点的值")
	private String parent;

	@ApiModelProperty("权限是否是叶子节点,true : 是叶子节点")
	private Boolean isLeaf;

	@ApiModelProperty("选中标记 true 选中 false 未选中")
	private Boolean checked;

	@ApiModelProperty("子权限集合")
	private List<PermModel> subPerms = new ArrayList<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PermModel permModel = (PermModel) o;
		return pval.equals(permModel.pval) &&
				pname.equals(permModel.pname) &&
				ptype.equals(permModel.ptype) &&
				parent.equals(permModel.parent) &&
				isLeaf.equals(permModel.isLeaf);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pval, pname, ptype, parent, isLeaf);
	}
}
