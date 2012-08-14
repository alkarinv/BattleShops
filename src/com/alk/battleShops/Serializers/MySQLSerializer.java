package com.alk.battleShops.Serializers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.Exceptions.SignFormatException;
import com.alk.battleShops.objects.Shop;
import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.SignValues;
import com.alk.battleShops.objects.Transaction;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.KeyUtil;
import com.alk.battleShops.util.Log;

/**
 * 
 * @author Alkarin
 *
 */
public class MySQLSerializer implements BCSStorageController{
	static final boolean DEBUG = false;

	Connection con;  /// Our database connection

	static public int MAX_NAME_LENGTH = 32;
	static public String URL = "localhost";
	static public String PORT = "3306";
	static public String USERNAME = "root";
	static public String PASSWORD = "";

	static public String DB = "minecraft";
	static public String TRANSACTION_TABLE = "transactions";
	static public String SHOPSIGN_TABLE = "signs";
	static public String SHOPCHEST_TABLE = "chests";
	static public String PLAYERSHOP_TABLE = "shops";
	static public String SHOPPERMISSION_TABLE = "associates";

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
	static final public String ITEMID = "ItemID";
	static final public String ITEMIDS = "ItemIDs";
	static final public String PERMISSIONS = "Pemissions";
	static final public String DATE = "Date";

	String mysql_create_database; 

	final String mysql_shopsign_table_exists = "desc " + SHOPSIGN_TABLE;
	final String mysql_shopchest_table_exists = "desc " + SHOPCHEST_TABLE;
	final String mysql_shoppermissions_table_exists = "desc " + SHOPPERMISSION_TABLE;
	final String mysql_transaction_table_exists = "desc " + TRANSACTION_TABLE;


	final String mysql_create_pk_table = "CREATE TABLE IF NOT EXISTS " + SHOPSIGN_TABLE +" ("+
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			X + " INTEGER ," +
			Y + " INTEGER ," +
			Z + " INTEGER ," +
			"PRIMARY KEY (" + WORLD + "," + X +", " + Y + "," + Z + ")) " +
			"DEFAULT CHARACTER SET = utf8 "+
			"COLLATE = utf8_general_ci";

	final String mysql_create_transaction_table = "CREATE TABLE IF NOT EXISTS " + TRANSACTION_TABLE +" ("+
			ID + " INTEGER NOT NULL AUTO_INCREMENT," +
			P1 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			BUY_OR_SELL + " TINYINT NOT NULL ,"+
			ITEMID + " INTEGER ," +
			QUANTITY + " INTEGER ," +
			PRICE + " DOUBLE ," +
			DATE + " DATETIME," +
			"PRIMARY KEY (" + ID + "), INDEX USING BTREE (" + P1 +"),INDEX USING BTREE (" + P2 +"))"+
			"DEFAULT CHARACTER SET = utf8 "+
			"COLLATE = utf8_general_ci";

	final String mysql_create_total_table = "CREATE TABLE IF NOT EXISTS " + SHOPCHEST_TABLE +" ("+
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			WORLD + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			X + " INTEGER ," +
			Y + " INTEGER ," +
			Z + " INTEGER ," +
			ITEMIDS + " VARCHAR(1024) NOT NULL ,"+
			"PRIMARY KEY (" + WORLD + "," + X +", " + Y + "," + Z + ")) " +
			"DEFAULT CHARACTER SET = utf8 "+
			"COLLATE = utf8_general_ci";

	final String mysql_create_permissions_table = "CREATE TABLE IF NOT EXISTS " + SHOPPERMISSION_TABLE +" ("+
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			P2 + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			"PRIMARY KEY (" + PLAYER + "," + P2 + ")) " +
			"DEFAULT CHARACTER SET = utf8 "+
			"COLLATE = utf8_general_ci";

	final String mysql_getall_signs = "select " +PLAYER+"," + WORLD +"," + X+","+Y+","+Z +" from " + SHOPSIGN_TABLE ;
	final String mysql_getall_chests = "select " +PLAYER+"," + WORLD +"," + X+","+Y+","+Z +"," + ITEMIDS + " from " + SHOPCHEST_TABLE ;
	final String mysql_getall_shopassociates = "select " +PLAYER+"," + P2+ " from " + SHOPPERMISSION_TABLE ;

	final String mysql_bulk_insert_shopchest = "INSERT INTO "+SHOPCHEST_TABLE+" VALUES %s ON DUPLICATE KEY UPDATE " +
			PLAYER + " = VALUES(" + PLAYER + ")" +"," + ITEMIDS +"= VALUES(" + ITEMIDS + ")";

	final String mysql_bulk_insert_shopsign = "INSERT INTO "+SHOPSIGN_TABLE+" VALUES %s ON DUPLICATE KEY UPDATE " +
			PLAYER + " = VALUES(" + PLAYER + ")";

	final String mysql_bulk_insert_shopassociates = "INSERT IGNORE INTO "+SHOPPERMISSION_TABLE+" VALUES %s ";

	final String mysql_bulk_insert_transactions = "INSERT INTO "+TRANSACTION_TABLE+" VALUES %s " ;

	final String mysql_delete_sign = "delete from " + SHOPSIGN_TABLE+" where " + WORLD +"='%s' and " + X + "=%s and "+Y+"=%s and " + Z +"=%s LIMIT 1";
	final String mysql_delete_chest = "delete from " + SHOPCHEST_TABLE+" where " + WORLD +"='%s' and " + X + "=%s and "+Y+"=%s and " + Z +"=%s LIMIT 1";
	final String mysql_delete_associate = "delete from " + SHOPPERMISSION_TABLE+" where " + PLAYER+"='%s' and " + P2 + "='%s' LIMIT 1";

	final String get_player_transactions = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P2+"='%s'";
	final String get_player_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P2+"='%s' AND "+DATE+
			" >= (CURDATE() - INTERVAL %s DAY )";

	final String get_shop_transactions = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P1+"='%s'";
	final String get_shop_transactions_ndays = "SELECT * FROM "+TRANSACTION_TABLE+" WHERE "+P1+"='%s' AND "+DATE+
			" >= (CURDATE() - INTERVAL %s DAY )";

	public MySQLSerializer(){}

	public boolean initted(){
		return con != null;
	}
	public boolean init(){
		mysql_create_database = "CREATE DATABASE IF NOT EXISTS " + DB;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			if (DEBUG) System.out.println("Got Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("Failed getting driver");
			e1.printStackTrace();
			return false;
		}

		String strStmt = mysql_create_database;
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT, USERNAME,PASSWORD);
			Statement st = con.createStatement();
			st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Creating db");
		} catch (SQLException e) {
			System.err.println("Failed creating db: "  + strStmt);
			e.printStackTrace();
			return false;
		}
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT +"/" + DB, USERNAME,PASSWORD);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}

		createTable(mysql_shopsign_table_exists, mysql_create_pk_table,null);
		createTable(mysql_shopchest_table_exists, mysql_create_total_table,null);
		createTable(mysql_shoppermissions_table_exists, mysql_create_permissions_table,null);
		createTable(mysql_transaction_table_exists, mysql_create_transaction_table,null);

		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean createTable(String sql_table_exists,
			String sql_create_table,String sql_create_index) {
		String strStmt;
		strStmt = sql_table_exists;
		/// Check to see if our table exists;
		boolean table_exists = false;
		try {
			Statement st = con.createStatement();
			st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("table exists");
			table_exists = true;
		} catch (SQLException e) {
			if (DEBUG) System.out.println("table does not exist");
		}
		/// If the table exists nothing left to do
		if (table_exists)
			return true;
		/// Create our table and index
		strStmt = sql_create_table;
		Statement st = null;
		int result =0;
		try {
			st = con.createStatement();

			result = st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Created Table with stmt=" + strStmt);

			if (sql_create_index != null){
				try{
					st = con.createStatement();
					st.executeUpdate(sql_create_index);
					if (DEBUG) System.out.println("Created Index");				
				} catch (Exception e){
					if (DEBUG) System.err.println("Failed in creating Index");
					return false;
				}					
			}
		} catch (Exception e) {
			if (DEBUG) System.err.println("Failed in creating Table " +
					strStmt + "   result=" + result);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private ResultSet executeQuery(String strRawStmt, Object... varArgs){
		StringBuilder buf = new StringBuilder();
		Formatter form = new Formatter(buf);
		form.format(strRawStmt, varArgs);

		try {
			Statement st = con.createStatement();
			if (DEBUG)System.out.println("Executing   =" + buf.toString());
			st.executeQuery(buf.toString());
			return st.getResultSet();
		} catch (SQLException e) {
			System.err.println("Couldnt execute query "  + buf.toString());
			e.printStackTrace();
			return null;
		}
	}

	private int executeUpdate(String strRawStmt, Object... varArgs){
		StringBuilder buf = new StringBuilder();
		Formatter form = new Formatter(buf);
		try{
			form.format(strRawStmt, varArgs);
		} catch (MissingFormatArgumentException e){
			System.err.println("Failed with stmt= " + strRawStmt + "   varArgs=" + varArgs);
			e.printStackTrace();
		}

		try {
			Statement st = con.createStatement();
			if (DEBUG) System.out.println("Executing   =" + buf.toString());
			return st.executeUpdate(buf.toString());
		} catch (SQLException e) {
			System.err.println("Couldnt execute update "  + buf.toString());
			e.printStackTrace();
			return -1;
		}
	}


	private void saveShops(){
		Map<World,Map<String, Shop>> allshops = WorldShop.getAllShops();
		if (allshops == null || allshops.size() <= 0)
			return;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		boolean found_associate= false;
		for (World w: allshops.keySet()){
			Map<String,Shop> shops = allshops.get(w);
			for (String player : shops.keySet()){
				Set<String> associates = shops.get(player).getAssociates();
				if (associates == null)
					continue;
				for (String associate : associates){
					found_associate = true;
					if (!first)
						sb.append(",");
					sb.append( "('" + player+"','" + associate + "')");
					first = false;
				}
			}			
		}
		if (found_associate){
			executeUpdate(mysql_bulk_insert_shopassociates, sb.toString());
			commit();
		}
	}

	public void loadShops(){
		ResultSet rs = executeQuery(mysql_getall_shopassociates);
		if (rs == null) return;
		try{
			Map<String,Set<String>> shopsWithAssociates  = new HashMap<String, Set<String>>();
			while (rs.next()){
				String player = rs.getString(PLAYER);
				String p2 = rs.getString(P2);
				Set<String> associates = shopsWithAssociates.get(player);
				if (associates == null){
					associates = new HashSet<String>();
					shopsWithAssociates.put(player, associates);
				}
				associates.add(p2);
			}

			Map<World , Map<String,Shop>> allshops = WorldShop.getAllShops();
			for (World w: allshops.keySet()){
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

		}
	}

	private void saveSigns(){
		Map<World, Map<String, ShopSign>> allsigns = WorldShop.getAllSigns();
		StringBuilder sb = new StringBuilder();
		boolean first =true;
		for (World w: allsigns.keySet()){
			Map<String, ShopSign> map = allsigns.get(w);
			if (map == null || map.isEmpty())
				continue;
			for (ShopSign ss : map.values()){
				String player = ss.getOwner().getName();
				String world = ss.getWorld().getName();
				int x= ss.getX();
				int y = ss.getY();
				int z = ss.getZ();
				if (!first) sb.append(",");
				sb.append( "('" + player + "','" + world+ "'," + x + "," + y + "," + z+ ")");
				first = false;
			}			
		}
		if (!first){
			executeUpdate(mysql_bulk_insert_shopsign, sb.toString());
			commit();
		}
	}

	private void saveChests(){
		Map<World, Map<String, ShopChest>> allchests = WorldShop.getAllChests();
		if (allchests == null || allchests.isEmpty())
			return;
		StringBuilder sb = new StringBuilder();
		boolean first =true;
		for (World w: allchests.keySet()){
			Map<String, ShopChest> map = allchests.get(w);
			if (map == null || map.isEmpty())
				continue;
			for (ShopChest sc : map.values()){
				String player = sc.getOwner().getName();
				String world = sc.getWorld().getName();
				int x= sc.getX();
				int y = sc.getY();
				int z = sc.getZ();
				Set<Integer> ids = sc.getItemIds();
				StringBuilder sb2 = new StringBuilder();
				boolean first2 = true;
				for (Integer i : ids){
					if (!first2) sb2.append(",");
					sb2.append( i );
					first2 = false;
				}
				if (!first) sb.append(",");
				sb.append( "('" + player + "','" + world+ "'," + x + "," + y + "," + z+ ",'" + sb2.toString() + "')");
				first = false;
			}
		}
		if (!first){
			executeUpdate(mysql_bulk_insert_shopchest, sb.toString());
			commit();
		}
	}

	private boolean commit(){
		try {
			con.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	//	private class StringLocation{
	//		String worldname;
	//		int x,y,z;
	//		StringLocation(Location l){
	//			this.worldname = l.getWorld().getName();x = l.getBlockX(); y = l.getBlockY(); z= l.getBlockZ();
	//		}
	//		StringLocation(String worldname, int x, int y, int z){
	//			this.worldname = worldname; this.x = x; this.y = y; this.z = z;
	//		}
	//	}
	@SuppressWarnings("unused")
	public void loadChests(){
		ResultSet rs = executeQuery(mysql_getall_chests);
		if (rs == null) return;
		List<Location> bad_locs = new LinkedList<Location>();
		HashMap<String, ShopChest> map = new HashMap<String, ShopChest>();
		try{
			while (rs.next()){
				try{
					String player = rs.getString(PLAYER);
					String worldname = rs.getString(WORLD);
					int x = rs.getInt(X);
					int y = rs.getInt(Y);
					int z = rs.getInt(Z);
					String stritemids = rs.getString(ITEMIDS);
					World world = Bukkit.getServer().getWorld(worldname);
					if (world == null){
						Log.warn("Ignoring chest at " + toString(worldname,x,y,z) +" as world no longer exists");
						continue;
					}
					Block b = world.getBlockAt(x, y, z);
					final Material mat = b.getType();
//										System.out.println("" + worldname + ":" + x + ":" + y + ":" + z);

					if (!(mat.equals(Material.CHEST) )){
						if (Defaults.ERROR_LVL > 1)
							Log.warn("[BattleShops] Chest is no longer at " + toString(worldname,x,y,z) +"!");
						bad_locs.add(new Location(world,x,y,z));
						continue;}

					//					CraftChest chest = new CraftChest(b);
					Chest chest = (Chest) b.getState();
					ShopOwner owner = new ShopOwner(player);

					Set<Integer> itemids = new HashSet<Integer>();
					if (stritemids != ""){
						String[] strids = stritemids.split(",");

						for (String s: strids){
							int id = Integer.valueOf(s);
							if (id >= 0)
								itemids.add(id);
						}
					}
					ShopChest sc = new ShopChest(chest,owner,itemids);

					if (sc == null)
						continue;
					/// Make sure its away from a previous chest
					boolean tooclose = false;
					for (ShopChest psc : map.values()){
						try{
							if (!psc.getWorld().getName().equals( sc.getLocation().getWorld().getName()))
								continue;
							//						Location l = sc.getLocation();
							int dist = (psc.getY() == sc.getY()) ? (int) (Math.abs(psc.getX() - sc.getX()) + Math.abs(psc.getZ() - sc.getZ())): Integer.MAX_VALUE;

							//						double dist = psc.getLocation().distance(sc.getLocation());
							if (dist <=1){
								Log.warn("[BattleShops] Couldnt load chest, too close to another chest !!!! " + 
										toString(worldname,x,y,z) +"  " +  toString(psc));
								bad_locs.add(new Location(world,x,y,z));
								tooclose = true;
							}
						} catch(Exception e){
							tooclose=true;
						}
					}
					if (!tooclose)
						map.put(KeyUtil.getStringLoc(b), sc);

				} catch (Exception error){
					error.printStackTrace();
					continue;
				}
			}
			//			WorldShop.setShopChests(map);
		} catch (Exception e){
			e.printStackTrace();
		}
		for (ShopChest sc: map.values()){
			WorldShop.addShopChest(sc);
		}

		deleteChestLocations(bad_locs);
	}


	public void loadSigns(){
		ResultSet rs = executeQuery(mysql_getall_signs);
		if (rs == null) return;
		List<Location> bad_locs = new LinkedList<Location>();
		List<ShopSign> list = new ArrayList<ShopSign>();
		try{
			while (rs.next()){
				String worldname = null;
				Integer x = null,y = null,z = null;
				try{
					String player = rs.getString(PLAYER);
					worldname = rs.getString(WORLD);
					x = rs.getInt(X);
					y = rs.getInt(Y);
					z = rs.getInt(Z);
					World world = Bukkit.getServer().getWorld(worldname);
					if (world == null){
						Log.warn("Ignoring sign at " + toString(worldname,x,y,z) +" as that world no longer exists");
						continue;
					}
					Block b = world.getBlockAt(x, y, z);

					if (!(b.getState() instanceof Sign)){
						if (Defaults.ERROR_LVL > 1) Log.warn("[BattleShops] Sign no longer at !!!! " +
								toString(worldname,x,y,z) +"  blockType=" + b.getTypeId());
						bad_locs.add(new Location(world,x,y,z));
						continue;
					}
					Sign sign = (Sign) b.getState();
					ShopOwner owner = new ShopOwner(player);

					String lines[] = sign.getLines();
					SignValues sv = null;
					try {
						sv = ShopSign.parseShopSign(lines);
					} catch (SignFormatException e) {
						Log.warn("[BattleShops] couldnt reparse sign!!!! " + toString(worldname,x,y,z));
						sign.setLine(1, "0 : U");
						sign.setLine(3, "None");
						//						e.printStackTrace();
						bad_locs.add(new Location(world,x,y,z));
						continue;
					}
					ShopSign ss = new ShopSign(owner,sign,sv);
//					System.out.println("" + worldname + ":" + x + ":" + y + ":" + z + "     " + ss + "    owener=" + owner + "   sv=" +sv);

					list.add(ss);
				} catch (Exception error){
					Log.err("[BattleShops] couldn't reparse sign at " +toString(worldname,x,y,z));
					deleteSignLocation(worldname,x,y,z);
					error.printStackTrace();
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
				WorldShop.addShopSign(ss);
			} catch (Exception e){
				Log.err("Error adding shopsign " + ss +" to the shops ");
				e.printStackTrace();
			}
		}
		deleteSignLocations(bad_locs);
	}

	private static String toString(ShopChest psc) {
		return toString(psc.getWorld().getName(), psc.getX(), psc.getY(), psc.getZ());
	}
	public static String toString(String worldname, int x, int y, int z){
		return worldname +":" + x+":"+y+":"+z;
	}
	private void deleteChestLocations(List<Location> locs){
		if (Defaults.DEBUG_SHOP_PERSISTANCE) System.out.println("  bad_locs.size()=" + locs.size());
		for (Location l : locs){
			deleteChestLocation(l.getWorld(), (int) l.getX(), (int) l.getY(), (int) l.getZ());
		}
		commit();
	}

	private void deleteSignLocations(List<Location> locs){
		if (Defaults.DEBUG_SHOP_PERSISTANCE) System.out.println("  bad_locs.size()=" + locs.size());
		for (Location l : locs){
			deleteSignLocation(l.getWorld(), (int) l.getX(), (int) l.getY(), (int) l.getZ());
		}
		commit();
	}

	public void deleteShopSign(ShopSign ss){
		deleteSignLocation(ss.getWorld(),ss.getX(),ss.getY(),ss.getZ());
		commit();
	}

	private void deleteSignLocation(World world, int x, int y, int z) {
		executeUpdate(mysql_delete_sign, world.getName(),x,y,z);
	}

	private void deleteSignLocation(String world, int x, int y, int z) {
		executeUpdate(mysql_delete_sign, world,x,y,z);
	}

	private void deleteChestLocation(World world, int x, int y, int z) {
		executeUpdate(mysql_delete_chest, world.getName(),x,y,z);
	}

	public void deleteShopChest(ShopChest ss){
		executeUpdate(mysql_delete_chest, ss.getWorld().getName(),ss.getX(),ss.getY(),ss.getZ());		
		commit();
	}
	public void deleteChest(Chest ss){
		executeUpdate(mysql_delete_chest, ss.getWorld().getName(),ss.getX(),ss.getY(),ss.getZ());		
		commit();
	}

	public void saveAll() {
		saveSigns();
		saveChests();
		saveShops();
	}

	public void loadAll(){
		loadSigns();
		loadChests();
		loadShops();
		//		WorldShopJSONSerializer.reconstruct();
		WorldShop.updateAllSigns();
	}

	public void deleteAssociate(String p1, String p2) {
		executeUpdate(mysql_delete_associate, p1, p2);
		commit();
	}

	public void saveTransactions(Collection<Transaction> trs) {
		if (trs.isEmpty()){
			return;
		}
		StringBuilder sb = new StringBuilder();
		boolean first =true;
		for (Transaction tr : trs){
			if (!first) sb.append(",");
			int b = tr.buying ? 1 : 0;
			Timestamp ts = new Timestamp(tr.cal.getTimeInMillis());
			sb.append( "(NULL,'"+ tr.p1 +"','"+tr.p2  +"',"+b+"," + tr.itemid +"," + tr.quantity +"," + tr.price +",'" + ts +"')");
			first = false;
		}

		executeUpdate(mysql_bulk_insert_transactions, sb.toString());
		commit();
	}


	public List<Transaction> getPlayerTransactions(String name, Integer ndays) {
		ResultSet rs;
		BattleShops.getMyLogger().saveAll(); /// a kludge.. i'm using caching.. but if this happens I need to save
		if (ndays == null){
			ndays = 1;
		}
		rs = executeQuery(get_player_transactions_ndays,name,ndays);			
		return parseTransactionResults(rs);
	}


	public List<Transaction> getShopTransactions(String name, Integer ndays) {
		ResultSet rs;
		BattleShops.getMyLogger().saveAll(); /// a kludge.. i'm using caching.. but if this happens I need to save
		if (ndays == null){
			ndays = 1;
		}
		rs = executeQuery(get_shop_transactions_ndays,name,ndays);			
		return parseTransactionResults(rs);
	}

	private List<Transaction> parseTransactionResults(ResultSet rs){
		List<Transaction> trs = new ArrayList<Transaction>();

		if (rs == null) 
			return trs;
		try{
			while (rs.next()){
				String p1 = rs.getString(P1);
				String p2 = rs.getString(P2);
				boolean buying = rs.getInt(BUY_OR_SELL) > 0;
				int itemid = rs.getInt(ITEMID);
				int quantity = rs.getInt(QUANTITY);
				int price = rs.getInt(PRICE);
				Timestamp ts = rs.getTimestamp(DATE);
				Calendar cal = new GregorianCalendar();
				cal.setTimeInMillis(ts.getTime());
				Transaction tr = new Transaction(p1, p2, buying, itemid, quantity, price, cal);
				trs.add(tr);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return trs;		
	}
}
