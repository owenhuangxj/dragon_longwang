package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/4/13
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@TableName("t_active_area_grp")
public class ActiveAreaGrp extends Model<ActiveAreaGrp> {
	@TableId(type = IdType.AUTO)
	private Integer activeAreaGrpId;
	private Integer activeId;
	private Integer areaGrpId;

	public ActiveAreaGrp(Integer activeId, Integer areaGrpId) {
		this.activeId = activeId;
		this.areaGrpId = areaGrpId;
	}
}
