package com.trenska.longwang.entity.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 2019/7/4
 * 创建人:Owen
 */
@Data
@TableName("t_shift")
public class Shift extends Model<Shift> {
	@TableId(type = IdType.AUTO)
	private int shiftId;
	private String shiftName;
}
