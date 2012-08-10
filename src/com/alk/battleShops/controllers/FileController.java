package com.alk.battleShops.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.alk.battleShops.Defaults;

/**
 * 
 * @author alkarin
 *
 */
public class FileController {

	private static String BACKUP_FILE_EXTENSION = ".bk";
	
	public static void backupJSONFiles() {
		backupFile(Defaults.WORLDSHOP_JSON);
		backupFile(Defaults.SHOPCHESTS_JSON);
		backupFile(Defaults.SHOPSIGNS_JSON);
	}

	private static void backupFile(String file_name){
		/// Create our output directory
		File f = new File(file_name);
		if (f.exists()){
			File rndir = new File(file_name + BACKUP_FILE_EXTENSION);
			if (rndir.exists()){
				rndir.delete();}
			f.renameTo(rndir);
		}
	}

	public static BufferedReader getBufferedReader(String name) throws FileNotFoundException {
		File f = new File(name);
		if (!f.exists()){
			f = new File(name + BACKUP_FILE_EXTENSION);
		}
		BufferedReader bfr = new BufferedReader(new FileReader(f));
		return bfr;
	}
	
	
}
