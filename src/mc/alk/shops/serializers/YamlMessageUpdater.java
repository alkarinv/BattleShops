package mc.alk.shops.serializers;

import java.io.File;
import java.io.IOException;

import mc.alk.plugin.updater.FileUpdater;
import mc.alk.plugin.updater.Version;

import org.bukkit.configuration.file.FileConfiguration;

public class YamlMessageUpdater {


	public void update(FileConfiguration config, File configFile, File backupDir) {
		if (!backupDir.exists()) backupDir.mkdir();

		Version curVersion = new Version(config.getString("version", "0"));
		Version newVersion = new Version("1.3.1");
		try{
			if (curVersion.compareTo(newVersion) < 0){
				curVersion = updateTo1Point31(configFile,backupDir, curVersion, newVersion);}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private Version updateTo1Point31(File oldFile, File backupDir, Version newVersion, Version oldVersion) throws IOException {
		FileUpdater fu = new FileUpdater(oldFile,backupDir,newVersion,oldVersion);
		fu.replace(".*version:.*", "version: "+newVersion.toString());
		fu.addBefore(".*no_admin_perms:.*",
				"    no_create_perms: '&cYou dont have permission to create a shop'",
				"    no_build_perms: '&cYou dont have permission to build in this area'");
		return fu.update();
	}

}
