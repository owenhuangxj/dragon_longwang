package com.trenska.longwang.enums;

/**
 * 2019/5/15
 * 创建人:Owen
 * 收款/付款状态
 */
public enum PaymentStat {

	INVALID("已作废",-1),
	CANCELLED("已取消",0),
	WAIT_RECEIPT("待收款",1),
	RECEIPRED("已收款",2),
	WAIT_PAY("待付款",3),
	PAYED("已付款",4),
	WAIT_PAYBACK("待退款",5),
	PAYBACKED("已退款",6);

	private String name;
	private int index;
	private PaymentStat(String name, int index){
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
