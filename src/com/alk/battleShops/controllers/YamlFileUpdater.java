package com.alk.battleShops.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.FileConfiguration;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.util.Log;

public class YamlFileUpdater {

	public void updateMessageSerializer(MessageController ms) {
		FileConfiguration fc = ms.getConfig();

		double version = fc.getDouble("version",0);
		File dir = BattleShops.getSelf().getDataFolder();
		/// configVersion: 1.2, move over to new messages.yml
		/// this will delete their previous messages.yml
		if (version < 1.2){
			File backupdir = new File(dir+"/backups");
			if (!backupdir.exists()){
				backupdir.mkdir();}
			File messageFile = new File(dir+"/messages.yml");
			messageFile.renameTo(new File(dir+"/backups/messages.1.1.yml"));
			Log.warn("Updating to messages.yml version 1.2");
			Log.warn("If you had custom changes to messages you will have to redo them");
			Log.warn("But the old messages are saved as backups/messages.1.1.yml");
			move("/default_files/messages.yml",dir+"/messages.yml");
			MessageController.setConfig(new File(dir+"/messages.yml"));
		}
	}

	public File move(String default_file, String config_file) {
		File file = new File(config_file);
		try{
			InputStream inputStream = getClass().getResourceAsStream(default_file);
			OutputStream out=new FileOutputStream(config_file);
			byte buf[]=new byte[1024];
			int len;
			while((len=inputStream.read(buf))>0){
				out.write(buf,0,len);}
			out.close();
			inputStream.close();
		} catch (Exception e){
		}
		return file;
	}

}
