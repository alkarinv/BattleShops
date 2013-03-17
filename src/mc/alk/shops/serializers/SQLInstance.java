package mc.alk.shops.serializers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.mc.MCBlock;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCServer;
import mc.alk.mc.MCWorld;
import mc.alk.mc.StringLocation;
import mc.alk.mc.blocks.MCChest;
import mc.alk.mc.blocks.MCSign;
import mc.alk.mc.factories.ItemFactory;
import mc.alk.serializers.SQLSerializer;
import mc.alk.shops.BattleShops;
import mc.alk.shops.Defaults;
import mc.alk.shops.controllers.Shop;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.controllers.SignParser;
import mc.alk.shops.controllers.TransactionLogger;
import mc.alk.shops.objects.EverythingItem;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.objects.SignFormatException;
import mc.alk.shops.objects.SignValues;
import mc.alk.shops.objects.Transaction;
import mc.alk.shops.utils.KeyUtil;
import mc.alk.util.Log;


/**
 *
 * @author Alkarin
 *
 */
public class SQLInstance extends SQLSerializer implements ShopsSerializer{
	static final boolean DEBUG = true;

	static public int MAX_NAME_LENGTH = 32;
	static public String URL = "localhost";
	static public String PORT = "3306";
	static public String USERNAME = "root";
	static public String PASSWORD = "";

	static public String DB = "minecraft";
	static public String TRANSACTION_TABLE = "shop_transactions";
	static public String SHOPSIGN_TABLE = "shop_signs";
	static public String SHOPCHEST_TABLE = "shop_chests";
	static public String SHOPCHESTITEM_TABLE = "shop_chestitems";
	static public String PLAYERSHOP_TABLE = "shop_shops";
	static public String SHOPPERMISSION_TABLE = "shop_associates";

	static final public String ID = "ID";
	static final public String PLAYER = "Player";
	static final public String P1 = "Player1";
	static final public String P2 = "Player2";
	static final public String BUYER = "Buyer";
	static final public String SELLER = "Seller";
	static final public String QUANTITY = "Quantity";
	static final public String PRICE = "Price";
	static final public String BUY_OR_SELL = "Type";
	static final public String X = "x";
	static final public String Y = "y";
	static final public String Z = "z";
	static final public String WORLD = "World";
	static final public String ITEM_ID = "ItemID";
	static final public String ITEM_DATA = "ItemData";
	static final public String PERMISSIONS = "Pemissions";
	static final public String DATE = "Date";
	static final public String SPECIAL = "Special";

	String sql_create_database;

	String sql_create_pk_table = "CREATE TABLE IF NOT EXISTS " + SHOPSIGN_TABLE +" ("+
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			X + " INTEGER ," +
			Y + " INTEGER ," +
			Z + " INTEGER ," +
			SPECIAL + " INTEGER DEFAULT 0 NOT NULL," +
			"PRIMARY KEY (" + WORLD + "," + X +", " + Y + "," + Z + "," + SPECIAL+")) ";

	String sql_create_transaction_table;

	String sql_create_total_table = "CREATE TABLE IF NOT EXISTS " + SHOPCHEST_TABLE +" ("+
			//			ID + " INTEGER NOT NULL AUTO_INCREMENT," +
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			X + " INTEGER ," +
			Y + " INTEGER ," +
			Z + " INTEGER ," +
			ITEM_ID + " INTEGER NOT NULL," +
			ITEM_DATA + " INTEGER NOT NULL," +
			SPECIAL + " INTEGER DEFAULT 0 NOT NULL," +
			"PRIMARY KEY ("+WORLD+ ","+X+", "+Y+","+Z+","+ITEM_ID+","+ITEM_DATA+","+SPECIAL+")) ";

	String sql_create_permissions_table = "CREATE TABLE IF NOT EXISTS " + SHOPPERMISSION_TABLE +" ("+
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			"PRIMARY KEY (" + PLAYER + "," + P2 + ")) ";

	final String sql_getall_signs = "select " +PLAYER+"," + WORLD +"," + X+","+Y+","+Z +" from " + SHOPSIGN_TABLE ;
	final String sql_getall_shopassociates = "select " +PLAYER+"," + P2+ " from " + SHOPPERMISSION_TABLE ;

	final String sql_getall_chests = "select " +PLAYER+"," + WORLD +"," + X+","+Y+","+Z +"," +
			ITEM_ID +"," +ITEM_DATA + " from " + SHOPCHEST_TABLE ;

	String sql_insert_shopchest = "INSERT INTO "+SHOPCHEST_TABLE+" VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
			PLAYER + " = VALUES(" + PLAYER + ")" +"," +
			ITEM_ID + " = VALUES(" + ITEM_ID + ")" +"," +
			ITEM_DATA + " = VALUES(" + ITEM_DATA + ")";

	String sql_insert_shopsign = "INSERT INTO "+SHOPSIGN_TABLE+" VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
			PLAYER + " = VALUES(" + PLAYER + ")";

	String sql_insert_shopassociates = "INSERT IGNORE INTO "+SHOPPERMISSION_TABLE+" VALUES (?,?) ";

	final String sql_insert_transactions = "INSERT INTO "+TRANSACTION_TABLE+" VALUES (?,?,?,?,?,?,?,?,?,?) " ;

	final String sql_delete_sign = "delete from " + SHOPSIGN_TABLE+" where " + WORLD +"=? and " + X + "=? and "+Y+"=? and " + Z +"=? ";
	final String sql_delete_chest = "delete from " + SHOPCHEST_TABLE+" where " + WORLD +"=? and " + X + "=? and "+Y+"=? and " + Z +"=? ";
	final String sql_delete_associate = "delete from " + SHOPPERMISSION_TABLE+" where " + PLAYER+"=? and " + P2 + "=? ";

	final String get_player_transactions = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P2+"=?";
	String get_player_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P2+"=? AND "+DATE+
			" >= (CURDATE() - INTERVAL ? DAY )";

	final String get_shop_transactions = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P1+"=?";
	String get_shop_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P1+"=? AND "+DATE+
			" >= (CURDATE() - INTERVAL ? DAY )";

	protected TransactionLogger transactionLogger;
	public SQLInstance(TransactionLogger transactionLogger){
		this.transactionLogger = transactionLogger;
	}

	@Override
	public boolean init() {
		if (!super.init())
			return false;
		switch(TYPE){
		case MYSQL:
			sql_create_transaction_table = "CREATE TABLE IF NOT EXISTS " + TRANSACTION_TABLE +" ("+
					ID + " INTEGER NOT NULL AUTO_INCREMENT," +
					P1 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					BUY_OR_SELL + " TINYINT NOT NULL ,"+
					ITEM_ID + " INTEGER ," +
					ITEM_DATA + " INTEGER ," +
					SPECIAL + " INTEGER ," +
					QUANTITY + " INTEGER ," +
					PRICE + " DOUBLE ," +
					DATE + " DATETIME," +
					"PRIMARY KEY (" + ID + "), INDEX USING BTREE (" + P1 +"),INDEX USING BTREE (" + P2 +"))";
			break;
		case SQLITE:
			sql_create_total_table = "CREATE TABLE IF NOT EXISTS " + SHOPCHEST_TABLE +" ("+
					PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					X + " INTEGER ," +
					Y + " INTEGER ," +
					Z + " INTEGER ," +
					ITEM_ID + " INTEGER NOT NULL," +
					ITEM_DATA + " INTEGER NOT NULL," +
					SPECIAL + " INTEGER DEFAULT 0 NOT NULL," +
					"PRIMARY KEY ("+WORLD+ ","+X+", "+Y+","+Z+","+ITEM_ID+","+ITEM_DATA+","+SPECIAL+")) ";

			sql_create_pk_table = "CREATE TABLE IF NOT EXISTS " + SHOPSIGN_TABLE +" ("+
					PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					X + " INTEGER ," +
					Y + " INTEGER ," +
					Z + " INTEGER ," +
					SPECIAL + " INTEGER DEFAULT 0 NOT NULL," +
					"PRIMARY KEY (" + WORLD + "," + X +", " + Y + "," + Z + ","+SPECIAL+ ")) ";

			sql_create_transaction_table = "CREATE TABLE IF NOT EXISTS " + TRANSACTION_TABLE +" ("+
					ID + " INTEGER NOT NULL ," +
					P1 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					BUY_OR_SELL + " TINYINT NOT NULL ,"+
					ITEM_ID + " INTEGER ," +
					ITEM_DATA + " INTEGER ," +
					SPECIAL + " INTEGER ," +
					QUANTITY + " INTEGER ," +
					PRICE + " DOUBLE ," +
					DATE + " DATETIME," +
					"PRIMARY KEY (" + ID + "))";
			//							" INDEX USING BTREE (" + P1 +"),INDEX USING BTREE (" + P2 +"))"+

			sql_create_permissions_table = "CREATE TABLE IF NOT EXISTS " + SHOPPERMISSION_TABLE +" ("+
					PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
					"PRIMARY KEY (" + PLAYER + "," + P2 + ")) ";

			sql_insert_shopchest = "INSERT OR REPLACE INTO "+SHOPCHEST_TABLE+" VALUES (?,?,?,?,?,?,?,?)";

			sql_insert_shopsign = "INSERT OR REPLACE INTO "+SHOPSIGN_TABLE+" VALUES (?,?,?,?,?,?)";

			sql_insert_shopassociates = "INSERT OR IGNORE INTO "+SHOPPERMISSION_TABLE+" VALUES (?,?) ";
			get_player_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P2+"=? AND "+DATE+
					" >= (julianday(date('now'))- ?)";

			get_shop_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P1+"=? AND "+DATE+
					" >=  (julianday(date('now'))- ?)";


			break;
		}
		if(shouldUpdateTo3point4()){
			updateTo3Point4();
		}

		Connection con =null;
		try {
			con = getConnection();  /// Our database connection
			createTable(con,SHOPSIGN_TABLE, sql_create_pk_table);
			createTable(con,SHOPCHEST_TABLE, sql_create_total_table);
			createTable(con,SHOPPERMISSION_TABLE, sql_create_permissions_table);
			createTable(con,TRANSACTION_TABLE, sql_create_transaction_table);
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally{
			closeConnection(con);
		}

		return true;
	}

	@Override
	public void saveAll() {
		saveSigns();
		saveChests();
		saveShops();
		saveTransactions();
	}

	@Override
	public void loadAll() {
		loadChests();
		loadSigns();
		loadShops();
		MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(),new Runnable(){
			/// Reconstruct signs after everything is loaded
			public void run() {
				ShopController.updateAllSigns();
			}
		},2000);
	}

	private void loadChests() {
		new Thread(new Runnable(){
			public void run() {
				final RSCon rscon = executeQuery(sql_getall_chests);
				if (rscon == null || rscon.rs == null)
					return;
				final ArrayList<Map<String,Object>> results = convertToResult(rscon);
				if (results == null)
					return;
				close(rscon);

				/// now resync with bukkit
				MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(),new Runnable(){
					public void run() {
						loadSyncChests(results);
					}
				});
			}
		}).start();
	}

	public void loadShops(){
		new Thread(new Runnable(){
			public void run() {
				final RSCon rscon = executeQuery(sql_getall_shopassociates);
				if (rscon == null || rscon.rs == null)
					return;
				final ArrayList<Map<String,Object>> results = convertToResult(rscon);
				if (results == null)
					return;
				close(rscon);

				/// now resync with bukkit
				MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(),new Runnable(){
					public void run() {
						loadSyncShops(results);
					}
				});
			}
		});
	}
	private void loadSyncChests(ArrayList<Map<String,Object>> results){
		List<StringLocation> bad_locs = new LinkedList<StringLocation>();
		HashMap<String, ShopChest> map = new HashMap<String, ShopChest>();
		try{
			for (Map<String,Object> rs: results){
				String worldname = null;
				Integer x = null,y = null ,z = null;
				try{
					String player = getString(rs,PLAYER);
					worldname = getString(rs,WORLD);
					x = getInt(rs,X);
					y = getInt(rs,Y);
					z = getInt(rs,Z);
					int itemid = getInt(rs,ITEM_ID);
					int datavalue = getInt(rs,ITEM_DATA);
					//					String stritemids = getString(rs,ITEMIDS);
					MCWorld world = MCServer.getWorld(worldname);
					if (world == null){
						bad_locs.add(new StringLocation(worldname,x,y,z));
						Log.warn("Ignoring chest at " + toString(worldname,x,y,z) +" as world no longer exists");
						continue;
					}
					MCBlock b = world.getBlockAt(x, y, z);
					MCChest chest = (MCChest) world.toType(b, MCChest.class);
					if (chest == null){
						bad_locs.add(new StringLocation(worldname,x,y,z));
						Log.warn("Ignoring chest at " + toString(worldname,x,y,z) +" as chest no longer exists");
						continue;
					}

					ShopOwner owner = new ShopOwner(player);
					ShopChest sc = map.get(KeyUtil.getStringLoc(b));

					//					ShopChest sc = ShopController.getShopChest(chest);
					if (sc == null){
						sc = new ShopChest(chest,owner,new ArrayList<MCItemStack>());
					}
					if (!EverythingItem.isEverythingID(itemid,(short)datavalue)){
						sc.addItemID(ItemFactory.createItem(itemid, (short) datavalue, 1));
					} else {
						sc.addItemID(EverythingItem.EVERYTHING_ITEM);
					}
					map.put(KeyUtil.getStringLoc(b), sc);
					//					ShopController.addShopChest(sc);
				} catch (Exception error){
					bad_locs.add(new StringLocation(worldname,x,y,z));
					error.printStackTrace();
					continue;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		for (ShopChest sc: map.values()){
			ShopController.addShopChest(sc);
		}

		deleteChestLocations(bad_locs);
	}
	private void loadSyncShops(ArrayList<Map<String, Object>> results){
		try{
			Map<String,Set<String>> shopsWithAssociates  = new HashMap<String, Set<String>>();
			for (Map<String,Object> rs: results){
				String player = getString(rs,PLAYER);
				String p2 = getString(rs,P2);
				Set<String> associates = shopsWithAssociates.get(player);
				if (associates == null){
					associates = new HashSet<String>();
					shopsWithAssociates.put(player, associates);
				}
				associates.add(p2);
			}

			Map<MCWorld , Map<String,Shop>> allshops = ShopController.getAllShops();
			for (MCWorld w: allshops.keySet()){
				Map<String, Shop> shops = allshops.get(w);
				/// put in our associates
				for (String player: shopsWithAssociates.keySet()){
					//				System.out.println("loading in player=" + player);
					Shop s = shops.get(player);
					if (s == null){ /// This probably should be an error
						ShopOwner so = new ShopOwner(player);
						s = new Shop(so);
						shops.put(ShopOwner.getShopOwnerKey(so), s);
					}
					for (String associate : shopsWithAssociates.get(player)){
						s.addToAssociates(associate);
					}
				}
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public void loadSigns(){
		new Thread(new Runnable(){
			public void run() {
				final RSCon rscon = executeQuery(sql_getall_signs);
				if (rscon == null || rscon.rs == null)
					return;
				final ArrayList<Map<String,Object>> results = convertToResult(rscon);

				if (results == null || results.isEmpty())
					return;
				close(rscon);
				/// now resync with whatever API
				MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(),new Runnable(){
					@Override
					public void run() {
						loadSyncSigns(results);
					}
				});
			}

		}).start();
	}

	private void loadSyncSigns(ArrayList<Map<String,Object>> results){
		List<StringLocation> bad_locs = new LinkedList<StringLocation>();
		List<ShopSign> list = new ArrayList<ShopSign>();
		try{
			for (Map<String,Object> rs: results){
				String worldname = null;
				Integer x = null,y = null,z = null;
				try{
					String player = getString(rs,PLAYER);
					worldname = getString(rs,WORLD);
					x = getInt(rs,X);
					y = getInt(rs,Y);
					z = getInt(rs,Z);
					MCWorld world = MCServer.getWorld(worldname);
					if (world == null){
						Log.warn("Ignoring sign at " + toString(worldname,x,y,z) +" as that world no longer exists");
						bad_locs.add(new StringLocation(worldname,x,y,z));
						continue;
					}
					MCBlock b = world.getBlockAt(x, y, z);
					MCSign sign = (MCSign) world.toType(b, MCSign.class);
					if (sign == null){
						//						if (Defaults.ERROR_LVL > 1) Log.warn("[BattleShops] Sign no longer at !!!! " +
						//								toString(worldname,x,y,z) +"  blockType=" + b.getType());
						bad_locs.add(new StringLocation(worldname,x,y,z));
						continue;
					}
					ShopOwner owner = new ShopOwner(player);
					String lines[] = sign.getLines();
					SignValues sv = null;
					try {
						sv = SignParser.parseShopSign(lines);
					} catch (SignFormatException e) {
						Log.warn("[BattleShops] couldnt reparse sign!!!! " + toString(worldname,x,y,z));
						sign.setLine(1, "0 : U");
						sign.setLine(3, "None");
						bad_locs.add(new StringLocation(world.getName(),x,y,z));
						continue;
					}
					ShopSign ss = new ShopSign(owner,sign,sv);
					list.add(ss);
				} catch (Exception error){
					//					Log.err("[BattleShops] couldn't reparse sign at " +toString(worldname,x,y,z));
					//					deleteSignLocation(worldname,x,y,z);
					error.printStackTrace();
					if (worldname != null && x != null && y != null && z != null)
						bad_locs.add(new StringLocation(worldname,x,y,z));

					//					error.printStackTrace();
					continue;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		for (ShopSign ss: list){
			try{
				//				System.out.println("ss = " + ss);
				ShopController.addShopSign(ss);
			} catch (Exception e){
				Log.err("Error adding shopsign " + ss +" to the shops ");
				e.printStackTrace();
			}
		}
		deleteSignLocations(bad_locs);
	}

	private void saveSigns(){
		Map<MCWorld, Map<String, ShopSign>> allsigns = ShopController.getAllSigns();
		List<List<Object>> batch = new ArrayList<List<Object>>();
		for (MCWorld w: allsigns.keySet()){
			Map<String, ShopSign> map = allsigns.get(w);
			if (map == null || map.isEmpty())
				continue;
			for (ShopSign ss : map.values()){
				String player = ss.getOwner().getName();
				String world = ss.getWorld().getName();
				int x= ss.getX();
				int y = ss.getY();
				int z = ss.getZ();

				batch.add(Arrays.asList(new Object[]{
						player,world,x,y,z, ss.getItemStack().isSpecial()}));
			}
		}

		executeBatch(BattleShops.isEnabled(), sql_insert_shopsign, batch);
	}

	private void saveChests(){
		Map<MCWorld, Map<String, ShopChest>> allchests = ShopController.getAllChests();
		if (allchests == null || allchests.isEmpty())
			return;
		List<List<Object>> batch = new ArrayList<List<Object>>();

		for (MCWorld w: allchests.keySet()){
			Map<String, ShopChest> map = allchests.get(w);
			if (map == null || map.isEmpty())
				continue;
			for (ShopChest sc : map.values()){
				String player = sc.getOwner().getName();
				String world = sc.getWorld().getName();
				int x= sc.getX();
				int y = sc.getY();
				int z = sc.getZ();
				Collection<MCItemStack> ids = sc.getItemIds();
				//				if (sc.isEverythingChest()){
				//					batch.add(Arrays.asList(new Object[]{
				//							player,world,x,y,z,
				//							KeyUtil.toItemID(Defaults.EVERYTHING_ID),
				//							KeyUtil.toItemDataValue(Defaults.EVERYTHING_ID),
				//							0 }));
				//				} else {
				for (MCItemStack item : ids){
					batch.add(Arrays.asList(new Object[]{
							player,world,x,y,z,
							item.getType(), item.getDataValue(), item.isSpecial() }));
				}
				//				}
			}
		}
		executeBatch(BattleShops.isEnabled(), sql_insert_shopchest, batch);
	}

	private void saveShops(){
		Map<MCWorld,Map<String, Shop>> allshops = ShopController.getAllShops();
		if (allshops == null || allshops.size() <= 0)
			return;

		List<List<Object>> batch = new ArrayList<List<Object>>();
		for (MCWorld w: allshops.keySet()){
			Map<String,Shop> shops = allshops.get(w);
			for (String player : shops.keySet()){
				Set<String> associates = shops.get(player).getAssociates();
				if (associates == null)
					continue;
				for (String associate : associates){
					batch.add(Arrays.asList(new Object[]{player,associate}));
				}
			}
		}

		executeBatch(BattleShops.isEnabled(), sql_insert_shopassociates, batch);
	}

	private void saveTransactions() {
		saveTransactions(transactionLogger.getTransactionsAndClear());
	}

	public static String toString(String worldname, int x, int y, int z){
		return worldname +":" + x+":"+y+":"+z;
	}

	@Override
	public String toString(){
		return "[SQLInstance " + getDB() +"  " + URL + "   " + PORT +"   " + USERNAME +"]";
	}

	private void deleteChestLocations(final List<StringLocation> locs){
		if (Defaults.DEBUG_SHOP_PERSISTANCE) System.out.println("  chest bad_locs.size()=" + locs.size());
		deleteLocations(sql_delete_chest,locs);
	}

	private void deleteSignLocations(final List<StringLocation> locs){
		if (Defaults.DEBUG_SHOP_PERSISTANCE) System.out.println("  sign bad_locs.size()=" + locs.size());
		deleteLocations(sql_delete_sign,locs);
	}

	private void deleteLocations(String stmt, final List<StringLocation> locs){
		List<List<Object>> batch = new ArrayList<List<Object>>();
		for (StringLocation l : locs){
			batch.add(Arrays.asList(new Object[]{l.getWorldName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()}));
		}
		executeBatch(BattleShops.isEnabled(), stmt, batch);
	}

	@Override
	public void deleteShopChest(ShopChest chest) {
		executeUpdate(BattleShops.isEnabled(), sql_delete_chest, chest.getWorld().getName(),
				chest.getX(),chest.getY(),chest.getZ());
	}

	@Override
	public void deleteShopSign(ShopSign ss) {
		executeUpdate(BattleShops.isEnabled(), sql_delete_sign, ss.getWorld().getName(),
				ss.getLocation().getBlockX(),ss.getLocation().getBlockY(),ss.getLocation().getBlockZ());
	}

	@Override
	public void deleteAssociate(String p1, String p2) {
		executeUpdate(sql_delete_associate, p1, p2);
	}

	@Override
	public void saveTransactions(Collection<Transaction> trs) {
		if (trs.isEmpty()){
			return;
		}
		List<List<Object>> batch = new ArrayList<List<Object>>();
		for (Transaction tr : trs){
			int b = tr.buying ? 1 : 0;
			Timestamp ts = new Timestamp(tr.cal.getTimeInMillis());
			batch.add(Arrays.asList(new Object[]{
					null,tr.p1,tr.p2,b,tr.itemid,tr.datavalue,tr.special,tr.quantity,tr.price,ts}));
		}
		executeBatch(BattleShops.isEnabled(),sql_insert_transactions, batch);
	}

	@Override
	public List<Transaction> getPlayerTransactions(String name, Integer ndays) {
		return getTransactions(name,ndays, get_player_transactions_ndays);
	}

	@Override
	public List<Transaction> getShopTransactions(String name, Integer ndays) {
		return getTransactions(name,ndays, get_shop_transactions_ndays);
	}

	private List<Transaction> getTransactions(String name, Integer ndays, String sqlStatement){
		RSCon rs;
		if (ndays == null){
			ndays = 1;}
		saveTransactions();
		rs = executeQuery(sqlStatement,name,ndays);
		return parseTransactionResults(rs);
	}

	private List<Transaction> parseTransactionResults(RSCon rscon){
		List<Transaction> trs = new ArrayList<Transaction>();
		if (rscon == null || rscon.rs == null)
			return trs;

		try{
			ResultSet rs = rscon.rs;
			while (rs.next()){
				String p1 = rs.getString(P1);
				String p2 = rs.getString(P2);
				boolean buying = rs.getInt(BUY_OR_SELL) > 0;
				int itemid = rs.getInt(ITEM_ID);
				int datavalue = rs.getInt(ITEM_DATA);
				int quantity = rs.getInt(QUANTITY);
				int price = rs.getInt(PRICE);
				Timestamp ts = rs.getTimestamp(DATE);
				Calendar cal = new GregorianCalendar();
				cal.setTimeInMillis(ts.getTime());
				Transaction tr = new Transaction(p1, p2, buying, itemid,
						datavalue, quantity, price, cal);
				trs.add(tr);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return trs;
	}

	@Override
	public void deleteShopChest(MCChest chest) {
		executeUpdate(BattleShops.isEnabled(),
				sql_delete_chest, chest.getWorld().getName(),chest.getX(),chest.getY(),chest.getZ());

	}

	public boolean shouldUpdateTo3point4() {
		return hasTable("transactions") && !hasTable(TRANSACTION_TABLE);
	}

	private void updateTo3Point4() {
		if (!shouldUpdateTo3point4())
			return;
		Log.warn("[BattleShops] updating database to 3.4");
		String rename2 = null;
		String rename3 = null;
		String rename4 = null;

		String alter1= "ALTER TABLE "+SHOPSIGN_TABLE+" ADD "+SPECIAL+" INTEGER DEFAULT 0 NOT NULL";
		String alter2 = "ALTER TABLE "+TRANSACTION_TABLE+" ADD "+ITEM_DATA+" INTEGER DEFAULT 0 NOT NULL AFTER ItemID";
		String alter3 = "ALTER TABLE "+TRANSACTION_TABLE+" ADD "+SPECIAL+" INTEGER DEFAULT 0 AFTER "+ITEM_DATA;

		switch(TYPE){
		case MYSQL:
			rename2 = "RENAME TABLE signs TO "+SHOPSIGN_TABLE;
			rename3 = "RENAME TABLE transactions TO "+TRANSACTION_TABLE;
			rename4 = "RENAME TABLE associates TO "+SHOPPERMISSION_TABLE;
			break;
		case SQLITE:
			rename2 = "ALTER TABLE signs RENAME TO "+SHOPSIGN_TABLE;
			rename4 = "ALTER TABLE associates RENAME TO "+SHOPPERMISSION_TABLE;
			break;
		}


		executeUpdate(rename2);
		executeUpdate(rename4);
		/// create the new table
		Connection con =null;
		try {
			con = getConnection();  /// Our database connection
			createTable(con,SHOPCHEST_TABLE, sql_create_total_table);
			if (TYPE == SQLType.SQLITE)
				createTable(con,TRANSACTION_TABLE, sql_create_transaction_table);
			closeConnection(con);
		} catch (SQLException e) {
		} finally{
			closeConnection(con);
		}
		String getall_chests = "select " +PLAYER+"," + WORLD +"," + X+","+Y+","+Z +", ItemIDS" +
				" from chests";

		final RSCon rscon = executeQuery(getall_chests);
		if (rscon == null || rscon.rs == null) return;
		final ArrayList<Map<String,Object>> results = convertToResult(rscon);
		close(rscon);
		/// move all the previous chests over to the new table
		List<List<Object>> batch = new ArrayList<List<Object>>();
		for (Map<String,Object> rs: results){
			try{
				String player = getString(rs,PLAYER);
				String worldname = getString(rs,WORLD);
				int x = getInt(rs,X);
				int y = getInt(rs,Y);
				int z = getInt(rs,Z);
				String stritemids = getString(rs,"ItemIDs");
				if (stritemids != ""){
					String[] strids = stritemids.split(",");
					for (String s: strids){
						int id = Integer.valueOf(s);
						if (id <= 0)
							continue;
						int type = id  % 100000;
						int datavalue = id / 100000;
						batch.add(Arrays.asList(new Object[]{
								player,worldname,x,y,z,
								type, datavalue, 0 }));
					}
				}
			} catch (Exception e){
				continue;
			}
		}
		executeBatch(false,sql_insert_shopchest, batch);

		executeUpdate(alter1);
		switch(TYPE){
		case MYSQL:
			executeUpdate(rename3);

			executeUpdate(alter2);
			executeUpdate(alter3);
			break;
		case SQLITE:
			String reinsert = "INSERT INTO "+TRANSACTION_TABLE+" SELECT " +
					ID+","+P1+ ","+P2+", "+BUY_OR_SELL+","+ITEM_ID+"%100000,"+ITEM_ID+"/100000,0,"+QUANTITY +
					","+PRICE+","+DATE+" FROM transactions;";
			executeUpdate(reinsert);
			//			executeUpdate("drop table transactions");
			break;
		}

		executeUpdate("drop table chests");
	}

}
