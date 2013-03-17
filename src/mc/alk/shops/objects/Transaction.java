package mc.alk.shops.objects;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Transaction {
	public String p1, p2;
	public boolean buying;
	public int itemid, datavalue, quantity;
	public int special;
	public float price;
	public Calendar cal;

	public Transaction(String p1,String p2, boolean buying, int itemid, int datavalue, int quantity, float price, Calendar cal){
		this.p1 = p1;
		this.p2 = p2;
		this.buying = buying;
		this.itemid = itemid;
		this.datavalue = datavalue;
		this.quantity = quantity;
		this.price = price;
		this.cal = cal;
	}
	public String getKey(){
		return p1 +":" + p2 +":" + buying +":" + itemid + ":" + cal.getTimeInMillis();
	}
	public void merge(Transaction t){
		this.quantity += t.quantity;
		this.price += t.price;
	}
	public String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
		return sdf.format(cal.getTimeInMillis());
	}

}
