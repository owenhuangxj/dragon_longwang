package com.trenska.longwang.jdbc.mapper;

import com.trenska.longwang.entity.sys.SysConfig;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 2019/4/14
 * 创建人:Owen
 */
public class SysConfigRowMapper implements RowMapper {
	@Override
	public SysConfig mapRow(ResultSet rs, int i) throws SQLException {
		SysConfig sysConfig = new SysConfig();
		sysConfig.setSysEmpId(rs.getInt("sys_emp_id"));
		sysConfig.setStockoutByMadedate(rs.getBoolean("stockout_way"));
		sysConfig.setGoodsImgable(rs.getBoolean("goods_imgable"));
		sysConfig.setRetain(rs.getInt("retain"));
		return sysConfig;
	}
}
