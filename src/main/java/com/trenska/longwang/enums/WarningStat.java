package com.trenska.longwang.enums;

/**
 * 2019/4/27
 * 创建人:Owen
 */
public enum WarningStat {

	NEW("新货",1),
	UNSALABLING("滞销风险商品",2),
	UNSALABLE("滞销商品",3),
	EXPIRING("临期商品",4),
	EXPIRED("过期商品",5);

	WarningStat(String name, Integer value) {
		this.name = name;
		this.value = value;
	}

	private String name;
	private Integer value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
}
