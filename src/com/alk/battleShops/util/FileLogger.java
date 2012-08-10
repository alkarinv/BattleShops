package com.alk.battleShops.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Vector;

import com.alk.battleShops.Defaults;


public class FileLogger {
	static Vector<String> msgs = new Vector<String>();
	
	public FileLogger() {}

	public static synchronized int log(String node, Object... varArgs) {
		try {
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd,hh:mm:ss");

		StringBuilder buf = new StringBuilder();
        Formatter form = new Formatter(buf);
        form.format(node, varArgs);
        msgs.add(sdf.format(cal.getTime()).toString() + "," + buf.toString() +"\n");
        return msgs.size();
		} catch(Exception e){
			e.printStackTrace();
		}
        return -1;
	}

	public static synchronized void saveAll() {
		try {
			FileWriter fstream = new FileWriter(new File(Defaults.LOG_LOC),true);
			BufferedWriter out = new BufferedWriter(fstream);
			for (String msg : msgs){
				out.write(msg);	
			}
			msgs.clear();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}

