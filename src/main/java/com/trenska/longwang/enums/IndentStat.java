package com.trenska.longwang.enums;

/**
 * 2019/4/22
 * 创建人:Owen
 */
public enum IndentStat {
	WAIT_CONFIRM("待审核",1),
	WAIT_STOCKOUT("待出库",2),
	STOCKOUTED("已出库",3),
	WAIT_AUDIT("待财审",4),
	AUDITED("已财审",5),
	FINISHED("已完成",6),
	CANCELLED("已取消",7),
	INVALID("已作废",8);

	private String name;
	private int index;
	private IndentStat(String name, int index){
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public static void main(String[] args){
		System.out.println(IndentStat.FINISHED.getName());
	}
}
