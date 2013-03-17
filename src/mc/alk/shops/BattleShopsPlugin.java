package mc.alk.shops;

import java.io.File;

import mc.alk.mc.MCPlugin;


public interface BattleShopsPlugin extends MCPlugin{

	File getDataDirectory();

	BattleShopsPlugin getSelf();

}
