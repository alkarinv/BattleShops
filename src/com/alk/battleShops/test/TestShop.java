package com.alk.battleShops.test;

import junit.framework.TestCase;

import com.alk.battleShops.Serializers.MySQLSerializer;
import com.alk.battleShops.objects.WorldShop;

public class TestShop extends TestCase {
	public void testAll(){
//		transactionTest();
		MySQLSerializer sql = new MySQLSerializer();
		sql.init();
		sql.loadAll();
		WorldShop.printShops();
	}
	public void transactionTest(){
		
	}
	
}
