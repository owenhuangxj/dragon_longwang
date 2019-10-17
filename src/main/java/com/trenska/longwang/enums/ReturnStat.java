package com.trenska.longwang.enums;

/**
 * 2019/5/15
 * 创建人:Owen
 */
public enum ReturnStat {
	OBSOLETE("已作废",-1),
	RETURENED("已退货",0),
	FINISHED("已完成",1);

	private String name;
	private int index;
	private ReturnStat(String name, int index){
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

}
