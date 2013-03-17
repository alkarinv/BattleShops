package mc.alk.shops.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import mc.alk.shops.objects.Transaction;

public class TransactionLogger {
	static Vector<String> msgs = new Vector<String>();
	public HashMap<String, Transaction> trs = new HashMap<String, Transaction>();

	public synchronized int log(String p1,String p2, boolean buying, int itemid,
			short datavalue, int quantity, float price) {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Transaction tr = new Transaction(p1,p2,buying,itemid, datavalue, quantity,price,cal);
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

	public Collection<Transaction> getTransactionsAndClear(){
		ArrayList<Transaction> ts = new ArrayList<Transaction>(trs.values());
		trs.clear();
		return ts;
	}
}

