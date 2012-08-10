package com.alk.battleShops.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import com.alk.battleShops.Serializers.BCSStorageController;
import com.alk.battleShops.objects.Transaction;


public class MyLogger {
	static Vector<String> msgs = new Vector<String>();
	public HashMap<String, Transaction> trs = new HashMap<String, Transaction>();

	
	BCSStorageController sc;
	public MyLogger(BCSStorageController sc) {
		this.sc = sc;
	}

	public synchronized int log(String p1,String p2, boolean buying, int itemid, int quantity, float price) {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
//		System.out.println("cal=" + cal.getTimeInMillis());
		Transaction tr = new Transaction(p1,p2,buying,itemid,quantity,price,cal);
		String key = tr.getKey();
		synchronized(trs){
			if (trs.containsKey(key)){
				Transaction tr2 = trs.get(key);
				tr2.merge(tr);
			} else {
				trs.put(key, tr);
			}
		}
        return -1;
	}

	public synchronized void saveAll() {
		synchronized(trs){
			sc.saveTransactions(trs.values());
			trs.clear();			
		}
	}

}

