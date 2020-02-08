package com.trenska.longwang.util;

import java.util.Random;

/**
 * 2020/2/8
 * 创建人:Owen
 */
public class TestBillsUtils {

	public static void testMakeBillNo(){
		for(int i = 0 ; i < 10 ; i++)
		for (int index = 1 ; index <= 2 ; index++ ) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(BillsUtil.makeBillNo("TEST", new Random().nextInt(5)));
				}
			}).start();

		}
	}

	public static void main(String[] args) {
		testMakeBillNo();
	}

}
