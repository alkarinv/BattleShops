package com.alk.battleShops;

/**
 *
 * @author alkarin
 *
 */
public class Defaults {
	// How long you have to execute a command after typing inside console
	public static final int SECONDS_FOR_COMMAND = 60;

	// How long does it take for signs to update after a chest inventory is right clicked
	public static final int SECONDS_TILL_CHESTUPDATE = 30;

	/// Sign Defaults
	public static final int MAX_NAME_ON_SIGN_LENGTH = 15;
	// The string of 'MAX_SHOPSIGN_QUANTITY : MAX_SHOPSIGN_REMAINING'  should be <= 15 chars
	public static final int MAX_SHOPSIGN_QUANTITY = 54*64; /// One double chest worth of items
	public static final int MAX_SHOPSIGN_REMAINING = 999999;
	public static final float MIN_BS_PRICE = 0.001f; /// minimum buy sell price
	public static final float MAX_BS_PRICE = 100000.0f; /// maximum buy sell price

	public static final int MULTIPLIER_LIMIT = 128;

	/// File Defaults
	public static final String PLUGIN_PATH = "plugins/BattleShops/";
	public static final String SHOPSIGNS_JSON = PLUGIN_PATH + "shopsigns.json";
	public static final String SHOPCHESTS_JSON = PLUGIN_PATH + "shopchests.json";
	public static final String WORLDSHOP_JSON = PLUGIN_PATH + "worldshop.json";
	public static final String SHOPSIGNS_BIN = PLUGIN_PATH + "shopsigns.bin";
	public static final String SHOPCHESTS_BIN = PLUGIN_PATH + "shopchests.bin";
	public static final String WORLDSHOP_BIN = PLUGIN_PATH + "worldshop.bin";

	public static final String CONFIGURATION_FILE = PLUGIN_PATH + "config.yml";
	public static final String PERMISSION_FILE = PLUGIN_PATH + "permissions.yml";
	public static final String MESSAGES_FILE = PLUGIN_PATH + "messages.yml";

	// Default files are found within the jar and must start from the absolute jar path with "/"
	public static final String DEFAULT_YML_PATH = "/default_files/";
	public static final String DEFAULT_CONFIGURATION_FILE = DEFAULT_YML_PATH + "config.yml";
	public static final String DEFAULT_PERMISSION_FILE = DEFAULT_YML_PATH + "permissions.yml";
	public static final String DEFAULT_MESSAGES_FILE = DEFAULT_YML_PATH + "messages.yml";

	/// An id for selling/buying everything, set to a large number that is unlikely to be used
	public static final Integer EVERYTHING_ID = 13377;
	public static final String EVERYTHING_NAME = "EVERYTHING";

	public static String ADMIN_NAME = "ADMIN SHOP";
	public static String ADMIN_STR = "ADMIN";
	public static String LANGUAGE = "english";

	public static final int SAVE_EVERY_X_SECONDS = 60*10;  /// Save Every 10 minutes

	public static final boolean SHOULD_LOG = true;
	public static final String LOG_LOC = PLUGIN_PATH + "linklog.txt";

	public static boolean MULTIWORLD = true;

	public static Integer WAND = 76;

	public static boolean DISABLE_PLAYER_SIGN_BREAK = false;

	/// Tracing for debugging/extension purposes
	public static final int ERROR_LVL = 2;  /// 1 for standard erros.  2 for errors about missing chests/signs
	public static final boolean DEBUG_TRACE = false;
	public static final boolean DEBUG_SHOP_PERSISTANCE = false;
	public static final boolean DEBUG_LINKING = false;
	public static final boolean DEBUG_TRANSACTION = false;

	public static final boolean DEBUG_WITHLOG = false;

}
