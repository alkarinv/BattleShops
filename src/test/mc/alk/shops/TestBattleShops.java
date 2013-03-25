package test.mc.alk.shops;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;
import mc.alk.bukkit.BukkitItemStack;
import mc.alk.bukkit.MCCommandSender;
import mc.alk.mc.MCInventory;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCServer;
import mc.alk.mc.blocks.MCChest;
import mc.alk.mc.blocks.MCSign;
import mc.alk.serializers.SQLSerializer.SQLType;
import mc.alk.shops.BattleShops;
import mc.alk.shops.BattleShopsPlugin;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.controllers.Shop;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.controllers.SignParser;
import mc.alk.shops.controllers.TransactionController;
import mc.alk.shops.controllers.TransactionLogger;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.objects.SignFormatException;
import mc.alk.shops.objects.SignValues;
import mc.alk.shops.objects.Transaction;
import mc.alk.shops.serializers.SQLInstance;
import mc.alk.shops.utils.CompositeMap;
import mc.alk.shops.utils.InventoryUtil;
import mc.alk.shops.utils.KeyUtil;
import mc.alk.shops.utils.WorldUtil;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import test.mc.alk.TestBlock;
import test.mc.alk.TestItemStack;
import test.mc.alk.TestLocation;
import test.mc.alk.TestPlayer;
import test.mc.alk.TestServer;
import test.mc.alk.TestWorld;
import test.mc.alk.blocks.TestChest;
import test.mc.alk.blocks.TestSign;


public class TestBattleShops extends TestCase {
	public static String DATABASE = "testdb";
	public static String USER = "root";
	public static String PWD = "";

	static TestServer api = new TestServer();
	static TestWorld w = new TestWorld("test");
	TestShop ts =null;
	static LinkController linker = new LinkController();
	TestSQL sql = null;

	ShopController sc = null;

	boolean enabled = false;

	public void testTransactions(){
		sql.setType(SQLType.MYSQL);
		assertTrue(sql.init());
		TransactionController tc = new TransactionController(sql.getTransactionLogger());
		MCPlayer player1 = new TestPlayer("shopowner", new TestLocation(w,0,0,0));
		MCPlayer player2 = new TestPlayer("buyerseller", new TestLocation(w,0,0,0));
		MCPlayer admin = new TestPlayer("someadmin", new TestLocation(w,0,0,0));
		TestPermController perms = (TestPermController)PermController.getPermController();
		perms.setAllPerms(admin,true);
		ShopOwner owner = new ShopOwner(player1.getName());

		//// Add chests
		MCChest chest = (MCChest) w.getBlockAt(100,0,0);
		System.out.println(chest.getItems());
		ShopChest shopchest = createShopChest(owner, chest);
		ShopController.addShopChest(shopchest);
		linker.chestRightClick(chest, player1);
		ShopController.printShops();

		//// Add signs
		MCSign sign1 = (MCSign) w.getBlockAt(0,0,100);
		sign1.setLine(0, owner.getName());
		ShopSign ss = createShopSign(sign1);
		ShopController.addShopSign(ss);
		ShopController.updateAffectedSigns(owner,ss);

		/// 2nd sign
		MCSign sign2 = (MCSign) w.getBlockAt(0,0,101);
		sign2.setLine(0, owner.getName());
		ss = createShopSign(sign2);
		ShopController.addShopSign(ss);
		ShopController.updateAffectedSigns(owner,ss);
		MCItemStack carrot = new TestItemStack(Material.CARROT_ITEM.getId());

		/// save them
		sql.saveAll();

		/// start with one item
		assertEquals(1, getQuantityRemaining(sign1.getLine(1)));
		assertEquals(1,shopchest.amount(carrot));
		assertEquals(0,player2.getInventory().getItemAmount(carrot));
		assertTrue(tc.buyFromShop(ss, player2, 1)); /// buy

		/// after buying
		assertEquals(0, getQuantityRemaining(sign1.getLine(1)));
		assertEquals(0,shopchest.amount(carrot));
		assertEquals(1,player2.getInventory().getItemAmount(carrot));
		assertFalse(tc.buyFromShop(ss, player2, 1)); /// no items to buy

		/// sell back to him
		assertTrue(tc.sellToShop(ss, player2, 1)); /// sell
		assertEquals(1, getQuantityRemaining(sign1.getLine(1)));
		assertEquals(1,shopchest.amount(carrot));
		assertEquals(0,player2.getInventory().getItemAmount(carrot));

		assertFalse(tc.sellToShop(ss, player2, 1)); /// nothing to sell

		/// save them
		List<Transaction> trs = sql.getShopTransactions(player1.getName(), 1);
		assertNotNull(trs);
		assertEquals(2,trs.size());

		trs = sql.getPlayerTransactions(player2.getName(), 1);
		assertNotNull(trs);
		assertEquals(2,trs.size());

		ShopController.removeChest(shopchest);
		ShopController.removeShopSign(sign1);
		ShopController.removeShopSign(sign2);
	}
//
//	public void testQuit(){
//		System.exit(1);
//	}
//	public void testEnchantedItems(){
//		TransactionController tc = new TransactionController(transactionLogger);
//		MCPlayer player1 = new TestPlayer("shopowner", new TestLocation(w,0,0,0));
//		MCPlayer player2 = new TestPlayer("buyerseller", new TestLocation(w,0,0,0));
//		MCPlayer admin = new TestPlayer("someadmin", new TestLocation(w,0,0,0));
//		TestPermController perms = (TestPermController)PermController.getPermController();
//		perms.setAllPerms(admin,true);
//		ShopOwner owner = new ShopOwner(player1.getName());
//
//		//// Add chests
//		MCChest chest = (MCChest) w.getBlockAt(11,0,0);
//		System.out.println(chest.getItems());
//		ShopChest shopchest = createShopChest(owner, chest);
//		ShopController.addShopChest(shopchest);
//		linker.chestClick(chest, player1, true);
//		ShopController.printShops();
//
//		//// Add signs
//		MCSign sign = (MCSign) w.getBlockAt(0,0,11);
//		sign.setLine(0, owner.getName());
//		ShopSign ss = createShopSign(sign);
//		ShopController.addShopSign(ss);
//		ShopController.updateAffectedSigns(owner,ss);
//		System.out.println("**********************   " + ss.getItemStack());
//
//		MCItemStack sword = new TestItemStack(Material.DIAMOND_SWORD.getId());
//		System.out.println("______________________ " + shopchest.getItems().get(0));
//		/// start with one item
//		assertEquals(1, getQuantity(sign.getLine(1)));
//		assertEquals(1,shopchest.amount(sword));
//		assertEquals(0,player2.getInventory().getItemAmount(sword));
//		assertTrue(tc.buyFromShop(ss, player2, 1)); /// buy
//
//		/// after buying
//		assertEquals(0, getQuantity(sign.getLine(1)));
//		assertEquals(0,shopchest.amount(sword));
//		assertEquals(1,player2.getInventory().getItemAmount(sword));
//		assertFalse(tc.buyFromShop(ss, player2, 1)); /// no items to buy
//
//		/// sell back to him
//		assertTrue(tc.sellToShop(ss, player2, 1)); /// sell
//		assertEquals(1, getQuantity(sign.getLine(1)));
//		assertEquals(1,shopchest.amount(sword));
//		assertEquals(0,player2.getInventory().getItemAmount(sword));
//
//		assertFalse(tc.sellToShop(ss, player2, 1)); /// nothing to sell
//		ShopController.removeChest(shopchest);
//		ShopController.removeShopSign(sign);
//
//	}

	public void testUpdateSQL(){

	}

	public void testEverythingChestLinkings(){
		TestServer.enableThreading(false);
		MCPlayer player = new TestPlayer("fuud", new TestLocation(w,0,0,0));
		ShopOwner owner = new ShopOwner(player);

		//// Add chests
		ShopChest shopchest = createShopChest(owner, (MCChest) w.getBlockAt(20,0,0));
		ShopChest shopchest2 = createShopChest(owner, (MCChest) w.getBlockAt(1,0,0));
		for (int i=0;i<27;i++){
			shopchest2.addItem(new TestItemStack(1, 64, (short) 0));
		}
		ShopController.addShopChest(shopchest);
		ShopController.addShopChest(shopchest2);

		// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		assertEquals(2,ShopController.addShopSign(createShopSign(sign)));
		sleep(100);

		ShopController.printShops();
		assertFalse(linker.activateChestShop(shopchest.getChest(), player));

		//relink chest
		assertTrue(linker.activateChestShop(shopchest.getChest(), player));
		assertEquals(1,shopchest.getItemIds().size());


		assertFalse(linker.activateChestShop(shopchest.getChest(), player));
	}

	public void testKeys(){
		TestItemStack is = new TestItemStack(Material.GRASS.getId());
		long id = KeyUtil.toKey(is);
		assertEquals(Material.GRASS.getId(), CompositeMap.getHOB(id));
		assertEquals(0, CompositeMap.getLOB(id));
		assertEquals(is.getType(), InventoryUtil.parseItemStack(
				CompositeMap.getHOB(id), (short) CompositeMap.getLOB(id)).getType());
		assertEquals(is.getDataValue(), InventoryUtil.parseItemStack(
				CompositeMap.getHOB(id), (short) CompositeMap.getLOB(id)).getDataValue());

		short dv = 15;
		is = new TestItemStack(Material.WOOL.getId(), 1, dv);
		id = KeyUtil.toKey(is);
		assertEquals(Material.WOOL.getId(), CompositeMap.getHOB(id));
		assertEquals(dv, CompositeMap.getLOB(id));
		assertEquals(is.getType(), InventoryUtil.parseItemStack(
				CompositeMap.getHOB(id), (short) CompositeMap.getLOB(id)).getType());
		assertEquals(is.getDataValue(), InventoryUtil.parseItemStack(
				CompositeMap.getHOB(id), (short) CompositeMap.getLOB(id)).getDataValue());

	}

	public void testBuySell(){
		TransactionController tc = new TransactionController(sql.getTransactionLogger());
		MCPlayer player1 = new TestPlayer("shopowner", new TestLocation(w,0,0,0));
		MCPlayer player2 = new TestPlayer("buyerseller", new TestLocation(w,0,0,0));
		MCPlayer admin = new TestPlayer("someadmin", new TestLocation(w,0,0,0));
		TestPermController perms = (TestPermController)PermController.getPermController();
		perms.setAllPerms(admin,true);
		ShopOwner owner = new ShopOwner(player1.getName());

		//// Add chests
		MCChest chest = (MCChest) w.getBlockAt(1,0,0);
		System.out.println(chest.getItems());
		ShopChest shopchest = createShopChest(owner, chest);
		ShopController.addShopChest(shopchest);
		linker.chestRightClick(chest, player1);
		ShopController.printShops();

		//// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		sign.setLine(0, owner.getName());
		ShopSign ss = createShopSign(sign);
		ShopController.addShopSign(ss);
		ShopController.updateAffectedSigns(owner,ss);
		MCItemStack grass = new TestItemStack(Material.GRASS.getId());

		/// start with one item
		assertEquals(1, getQuantityRemaining(sign.getLine(1)));
		assertEquals(1,shopchest.amount(grass));
		assertEquals(0,player2.getInventory().getItemAmount(grass));
		assertTrue(tc.buyFromShop(ss, player2, 1)); /// buy

		/// after buying
		assertEquals(0, getQuantityRemaining(sign.getLine(1)));
		assertEquals(0,shopchest.amount(grass));
		assertEquals(1,player2.getInventory().getItemAmount(grass));
		assertFalse(tc.buyFromShop(ss, player2, 1)); /// no items to buy

		/// sell back to him
		assertTrue(tc.sellToShop(ss, player2, 1)); /// sell
		assertEquals(1, getQuantityRemaining(sign.getLine(1)));
		assertEquals(1,shopchest.amount(grass));
		assertEquals(0,player2.getInventory().getItemAmount(grass));

		assertFalse(tc.sellToShop(ss, player2, 1)); /// nothing to sell

		ShopController.removeChest(shopchest);
		ShopController.removeShopSign(sign);
	}


	public void testChestInventory(){
		MCChest c = (MCChest) w.getBlockAt(1,0,0);
		MCInventory inv = c.getInventory();

		MCItemStack grass = new TestItemStack(Material.GRASS.getId());
		assertEquals(1, inv.getItemAmount(new TestItemStack(1)));
		assertEquals(1, inv.getItemAmount(new TestItemStack(1,1)));
		inv.addItem(grass);
		inv.addItem(grass);

		assertEquals(3, inv.getItemAmount(new TestItemStack(2)));
		assertEquals(3, inv.getItemAmount(new TestItemStack(2,1)));
		inv.addItem(new TestItemStack(1));
		assertEquals(2, inv.getItemAmount(new TestItemStack(1)));
		inv.removeItem(new TestItemStack(1));
		assertEquals(1, inv.getItemAmount(new TestItemStack(1)));
	}

	public void testPlayerLinkBreaking(){
		MCPlayer player1 = new TestPlayer("linker", new TestLocation(w,0,0,0));
		TestPlayer player2 = new TestPlayer("breaker", new TestLocation(w,0,0,0));
		TestPlayer admin = new TestPlayer("someadmin", new TestLocation(w,0,0,0));
		TestPermController perms = (TestPermController)PermController.getPermController();
		perms.setAllPerms(admin,true);

		ShopOwner owner = new ShopOwner(player1.getName());

		//// Add chests
		MCChest chest = (MCChest) w.getBlockAt(1,0,0);
		System.out.println(chest.getItems());
		ShopChest shopchest = createShopChest(owner, chest);
		ShopController.addShopChest(shopchest);
		assertFalse(linker.chestRightClick(chest, admin)); /// admins dont break
		assertFalse(linker.chestRightClick(chest, player1)); /// clicking their own chest does nothing
		assertTrue(linker.chestRightClick(chest, player2)); /// should break, they are opening
		assertNull(ShopController.getShopChest(chest)); //// should be broken
		assertTrue(linker.activateChestShop(chest, player1)); /// relink
		assertTrue(linker.activateChestShop(chest, admin)); /// admins dont break
		assertNotNull(ShopController.getShopChest(chest)); //// not broken
		ShopController.printShops();
	}


	public void testDatabase(){
		BattleShops.getSelf().setShopSerializer(sql);
		try {
			assertTrue(sql.init());
			sql.getConnection();
		} catch (SQLException e) {
			fail("Connection to db was bad");
			System.exit(1);
		}
	}

	public void testSignSQL(){
		sql.setType(SQLType.MYSQL);
		assertTrue(sql.init());

		enabled = false;
		TestServer.enableThreading(false);
		//// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		ShopController.addShopSign(createShopSign(sign));

		/// save them
		sql.saveAll();
		assertEquals(1, ShopController.getAllSigns().get(null).size());
		try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}

		ShopController.getAllSigns().clear();
		assertEquals(0, ShopController.getAllSigns().size());

		/// Check to see that loading still has the saved signs in db
		sql.loadAll();
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(1, ShopController.getAllSigns().get(null).size());

		/// Remove signs
		ShopController.removeShopSign(sign);
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(0, ShopController.getAllSigns().get(null).size());

		/// Check to see that no signs are in db
		sql.loadAll();
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(0, ShopController.getAllSigns().get(null).size());
	}

	public void testChestSQL(){
		sql.setType(SQLType.MYSQL);
		assertTrue(sql.init());

		TestServer.enableThreading(false);
		ShopOwner owner = new ShopOwner("fuud");
		//// Add chests
		MCChest chest = (MCChest) w.getBlockAt(1,0,0);
		ShopChest shopchest = createShopChest(owner, chest);
		ShopController.addShopChest(shopchest);

		/// save them
		sql.saveAll();
		assertEquals(1, ShopController.getAllChests().get(null).size());
		try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}

		ShopController.getAllChests().clear();
		assertEquals(0, ShopController.getAllChests().size());

		/// Check to see that loading still has the them saved
		sql.loadAll();
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(1, ShopController.getAllChests().get(null).size());

		/// Remove chests
		ShopController.removeChest(shopchest);
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		assertEquals(0, ShopController.getAllChests().get(null).size());

		/// Check to see that no chests are in db
		sql.loadAll();
		sleep(100);

		assertEquals(0, ShopController.getAllChests().get(null).size());
	}
	public void sleep(long millis){
		try {Thread.sleep(millis);} catch (InterruptedException e) {e.printStackTrace();}
	}

	public void testSingleChestLinkings(){
		TestServer.enableThreading(false);
		MCPlayer player = new TestPlayer("fuud", new TestLocation(w,0,0,0));
		ShopOwner owner = new ShopOwner(player);

		//// Add chests
		ShopChest shopchest = createShopChest(owner, (MCChest) w.getBlockAt(1,0,0));
		ShopChest shopchest2 = createShopChest(owner, (MCChest) w.getBlockAt(4,0,0));

		ShopController.addShopChest(shopchest);
		ShopController.addShopChest(shopchest2);

		// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		ShopController.addShopSign(createShopSign(sign));
		sleep(100);

		ShopController.printShops();
		assertFalse(linker.activateChestShop(shopchest.getChest(), player));
		assertFalse(linker.activateChestShop(shopchest2.getChest(), player));

		assertTrue(linker.activateChestShop(shopchest.getChest(), player));
		assertFalse(linker.activateChestShop(shopchest.getChest(), player));
	}

	public void testDoubleChestLinkings1(){
		sql.setType(SQLType.MYSQL);
		assertTrue(sql.init());

		TestServer.enableThreading(false);
		MCPlayer player = new TestPlayer("fuud", new TestLocation(w,0,0,0));
		ShopOwner owner = new ShopOwner(player);

		//// Add chests
		ShopChest shopchest = createShopChest(owner, (MCChest) w.getBlockAt(1,0,0));
		ShopChest shopchest2 = createShopChest(owner, (MCChest) w.getBlockAt(2,0,0));

		ShopController.addShopChest(shopchest);
		ShopController.addShopChest(shopchest2);
		sleep(100);

		// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		ShopController.addShopSign(createShopSign(sign)); /// linked and active
		sleep(100);

		assertFalse(linker.activateChestShop(shopchest.getChest(), player)); /// delink both
		Shop s = ShopController.getShop(player);
		assertEquals(0,s.getChests().size());
		assertNull(ShopController.getShopChest(shopchest.getChest()));
		assertNull(ShopController.getShopChest(shopchest2.getChest()));

		assertTrue(linker.activateChestShop(shopchest2.getChest(), player)); /// this should link them
		assertEquals(1,s.getChests().size());
		assertNotNull(ShopController.getShopChest(shopchest.getChest()));
		assertNotNull(ShopController.getShopChest(shopchest2.getChest()));

		LinkController.breakChestShop((MCChest) w.getBlockAt(new TestLocation(w,2,0,0)));
		w.addBlock(new TestBlock(new TestLocation(w,2,0,0),0)); /// replace the chest with air
		assertEquals(0,s.getChests().size());
		assertNull(ShopController.getShopChest(shopchest.getChest()));
		assertNull(ShopController.getShopChest(shopchest2.getLocation()));
	}

	public void testChestSignLinking(){
		TestSQL sql = getSQL();
		sql.init();
		sql.deleteDB(DATABASE);
		sql.init();

		enabled = false;
		TestServer.enableThreading(false);
		MCPlayer player = new TestPlayer("fuud", new TestLocation(w,0,0,0));
		ShopOwner owner = new ShopOwner(player);

		//// Add chests
		ShopChest shopchest = createShopChest(owner, (MCChest) w.getBlockAt(1,0,0));
		ShopChest shopchest2 = createShopChest(owner, (MCChest) w.getBlockAt(2,0,0));

		ShopController.addShopChest(shopchest);
		ShopController.addShopChest(shopchest2);
		sleep(100);

		// Add signs
		MCSign sign = (MCSign) w.getBlockAt(0,0,0);
		ShopController.addShopSign(createShopSign(sign)); /// linked and active
		sleep(100);

		assertFalse(linker.activateChestShop(shopchest.getChest(), player)); /// delink both
		Shop s = ShopController.getShop(player);
		assertEquals(0,s.getChests().size());
		assertNull(ShopController.getShopChest(shopchest.getChest()));
		assertNull(ShopController.getShopChest(shopchest2.getChest()));

		assertTrue(linker.activateChestShop(shopchest2.getChest(), player)); /// this should link them
		assertEquals(1,s.getChests().size());
		assertNotNull(ShopController.getShopChest(shopchest.getChest()));
		assertNotNull(ShopController.getShopChest(shopchest2.getChest()));

		LinkController.breakChestShop((MCChest) w.getBlockAt(new TestLocation(w,2,0,0)));
		w.addBlock(new TestBlock(new TestLocation(w,2,0,0),0)); /// replace the chest with air
		assertEquals(0,s.getChests().size());
		assertNull(ShopController.getShopChest(shopchest.getChest()));
		assertNull(ShopController.getShopChest(shopchest2.getLocation()));
	}

	private ShopChest createShopChest(ShopOwner owner, MCChest chest) {
		return new ShopChest(chest,owner);
	}

	public ShopSign createShopSign(MCSign sign){
		MCPlayer p = new TestPlayer(sign.getLine(0),sign.getLocation());
		ShopSign shopsign = null;
		try {
			SignValues sv = SignParser.parseShopSign(sign.getLines());
			shopsign = new ShopSign(new ShopOwner(p), sign, sv);
		} catch (SignFormatException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return shopsign;
	}

	public class TestSQL extends SQLInstance{
		public TestSQL(TransactionLogger transactionLogger) {
			super(transactionLogger);
		}

		public TransactionLogger getTransactionLogger() {
			return transactionLogger;
		}

		public void deleteDB(String db){
			this.executeUpdate("drop database "+db);
		}
	}

	public class TestWorldUtil extends WorldUtil{}

	public class TestPermController extends PermController{
		HashMap<String, HashMap<String, Boolean>> playerperms = new HashMap<String, HashMap<String, Boolean>>();
		HashSet<String> allperms = new HashSet<String>();

		HashMap<String, Boolean> getPlayerPerms(String player){
			HashMap<String,Boolean> perms = playerperms.get(player);
			if (perms ==  null){
				perms = new HashMap<String,Boolean>();
				playerperms.put(player, perms);
			}
			return perms;
		}
		public void setAllPerms(MCPlayer player, boolean all){
			if (all) allperms.add(player.getName());
			else allperms.remove(player.getName());
		}
		public boolean setPerm(MCPlayer player, String perm, boolean value){
			return getPlayerPerms(player.getName()).put(perm, value);
		}
		@Override
		public boolean hasMCPermission(MCCommandSender sender, String perm) {
			if (allperms.contains(sender.getName())) return true;
			Boolean result= getPlayerPerms(sender.getName()).get(perm);
			return result == null ? false : result;
		}
		@Override
		public boolean isMCAdmin(MCCommandSender sender) {
			if (allperms.contains(sender.getName())) return true;
			Boolean result= getPlayerPerms(sender.getName()).get(Defaults.PERM_ADMIN);
			return result == null ? false : result;
		}
		@Override
		public boolean hasMCBuildPerms(MCPlayer player, MCLocation location) {
			if (allperms.contains(player.getName())) return true;
			Boolean result= getPlayerPerms(player.getName()).get("shop.build");
			return result == null ? false : result;
		}
	}

	public class TestInventoryUtil extends InventoryUtil{
		@Override
		public void removeMCItem(MCInventory inventory, MCItemStack itemStack) {
			inventory.removeItem(itemStack);
		}

		@Override
		public String getMCCommonName(MCItemStack itemStack) {
			return itemStack.getType() + "";
		}

		@Override
		public MCItemStack parseMCItemStack(String text) {
			ItemStack is = mc.alk.bukkit.util.BukkitInventoryUtil.getItemStack(text);
			return is == null ? null : new BukkitItemStack(is);
		}

		@Override
		public int getMCItemAmount(MCInventory inventory, MCItemStack itemStack) {
			return inventory.getItemAmount(itemStack);
		}

		@Override
		public void addMCItem(MCPlayer player, MCItemStack itemStack) {
			player.getInventory().addItem(itemStack);
		}

		@Override
		public MCItemStack parseMCItemStack(int id, short datavalue) {
			ItemStack item = mc.alk.bukkit.util.BukkitInventoryUtil.getItemStack(id,datavalue);
			return item == null ? null : new BukkitItemStack(item);
		}

	}
	public class TestShop implements BattleShopsPlugin{

		public TestShop(){
			sql = getSQL();

			BattleShops.init(new TestServer(), this,
					new TestPermController(), new TestInventoryUtil(),
					sql,
					new TestWorldUtil()
			);
			assertTrue(sql.init());
			sql.deleteDB(DATABASE);
			BukkitMessageController.setConfig(new File("default_files/messages.yml"));
		}

		@Override
		public boolean isEnabled() {return enabled;}

		@Override
		public BattleShopsPlugin getSelf() {return this;}

		@Override
		public File getDataDirectory() {
			return new File("default_files");
		}
	}

	int getQuantity(String text){
		text = BukkitMessageController.decolorChat(text);
		String split[] = text.split(":");
//		System.out.println("-------------------- " + text +"      " + split[1]);
		if (split.length == 2){
			return new Integer(split[0].trim());
		} else {
			return new Integer(split[0].trim());
		}
	}

	int getQuantityRemaining(String text){
		text = BukkitMessageController.decolorChat(text);
		String split[] = text.split(":");
		System.out.println("-------------------- " + text +"      " + split[1]);
		if (split.length == 2){
			if (split[1].trim().equalsIgnoreCase("U")){
				return -1;}
			if (split[1].trim().equalsIgnoreCase("F")){
				return -2;}
			return new Integer(split[1].trim());
		} else {
			return new Integer(split[0].trim());
		}
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		MCServer.setInstance(api);

		ts = new TestShop();

		enabled = false;

		new TestSign(new TestLocation(w,0,0,0), new String[]{"fuud","1", "1 : 1", "GRASS"});
		new TestSign(new TestLocation(w,0,0,2), new String[]{"ADMIN","1", "3 : 2", "DIAMOND"});
		new TestSign(new TestLocation(w,0,0,11), new String[]{"fuud","1", "3 : 2", "E DIAMOND_SWORD"});
		new TestSign(new TestLocation(w,0,0,100), new String[]{"fuud","1", "3 : 2", "CARROT"});
		new TestSign(new TestLocation(w,0,0,101), new String[]{"fuud","1", "3 : 2", "CARROT"});
		new TestChest(new TestLocation(w,1,0,0),new TestItemStack(1), new TestItemStack(2,1) );
		new TestChest(new TestLocation(w,2,0,0),new TestItemStack(3), new TestItemStack(4) );
		new TestChest(new TestLocation(w,4,0,0),new TestItemStack(11), new TestItemStack(12) );
		new TestChest(new TestLocation(w,100,0,0),new TestItemStack(Material.CARROT_ITEM.getId() ));

		TestItemStack is = new TestItemStack(Material.DIAMOND_SWORD.getId(),1);
		is.addEnchantment(Enchantment.DAMAGE_ALL,3);

		MCChest chest = new TestChest(new TestLocation(w,11,0,0), is );
		new TestChest(new TestLocation(w,12,0,0), new TestItemStack(1) );

		new TestChest(new TestLocation(w,20,0,0));

		assertTrue(chest.isDoubleChest());
		sc = new ShopController();

		sql.setType(SQLType.MYSQL);
		assertTrue(sql.init());

	}

	public TestSQL getSQL(){
		if (sql == null){
			sql = new TestSQL(new TransactionLogger());
			sql.setDB(DATABASE);
			sql.setUsername(USER);
			sql.setPassword(PWD);
		}
		return sql;
	}
}
