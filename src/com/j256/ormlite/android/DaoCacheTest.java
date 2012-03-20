package com.j256.ormlite.android;

import java.util.ArrayList;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * User test which tests some cache issues.
 * 
 * <p>
 * <b>WARNING:</b> This does NOT extend our BaseDaoTest because it needs to start and stop stuff in the middle
 * </p>
 */
public class DaoCacheTest extends AndroidTestCase {

	private ConnectionSource connectionSource;
	private DatabaseHelper helper;
	private Dao<Account, String> accountDao;
	private Dao<Order, String> orderDao;

	private void start() {
		helper = new DatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
	}

	private void stop() throws Exception {
		if (helper != null) {
			helper.close();
			helper = null;
		}
		if (connectionSource != null) {
			connectionSource.close();
			connectionSource = null;
		}
	}

	public void testDaoCache() throws Exception {

		start();

		TableUtils.dropTable(connectionSource, Account.class, true);
		TableUtils.createTable(connectionSource, Account.class);
		TableUtils.dropTable(connectionSource, Order.class, true);
		TableUtils.createTable(connectionSource, Order.class);

		accountDao = helper.getDao(Account.class);
		accountDao.setObjectCache(true);
		orderDao = helper.getDao(Order.class);
		orderDao.setObjectCache(true);

		String accountName = "account";
		Account account = new Account(accountName);
		assertEquals(1, accountDao.create(account));

		Account account1 = accountDao.queryForId(accountName);
		assertNotSame(account, account1);

		String orderName = "order1";
		Order order = new Order(orderName);
		order.account = account1;
		assertEquals(1, orderDao.create(order));

		Account account2 = accountDao.queryForId(accountName);
		assertSame(account1, account2);

		Order order1 = account1.getOrders().get(0);
		assertEquals(order, order1);

		Order order2 = orderDao.queryForId(orderName);
		assertSame(order, order2);
		assertSame(order1, order2);

		stop();
		DaoManager.clearCache();
		// accountDao.clearObjectCache();
		// orderDao.clearObjectCache();
		BaseDaoImpl.clearAllInternalObjectCaches();
		start();

		accountDao = helper.getDao(Account.class);
		accountDao.setObjectCache(true);
		orderDao = helper.getDao(Order.class);
		orderDao.setObjectCache(true);

		Account account3 = accountDao.queryForId(accountName);
		assertNotSame(account, account3);
		assertNotSame(account1, account3);
		assertNotSame(account2, account3);

		Order order3 = account3.getOrders().get(0);
		assertNotNull(order3);
		assertNotSame(order, order3);
		assertNotSame(order1, order3);
		assertNotSame(order2, order3);

		Order order4 = orderDao.queryForId(orderName);
		assertNotNull(order4);
		assertNotSame(order, order4);
		assertNotSame(order1, order4);
		assertNotSame(order2, order4);
		assertSame(order3, order4);

		Order order5 = orderDao.queryForId(orderName);
		assertSame(order4, order5);

		stop();
	}

	/******* Model *******/
	public static class Account {
		@DatabaseField(id = true)
		public String name;
		@ForeignCollectionField
		public ForeignCollection<Order> orders;

		public Account() {
			// ORMLite needs a no-arg constructor
		}

		public Account(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public ArrayList<Order> getOrders() {
			return new ArrayList<Order>(orders);
		}
	}

	public static class Order {
		@DatabaseField(id = true)
		public String name;
		@DatabaseField(foreign = true)
		public Account account;

		public Order() {
			// ORMLite needs a no-arg constructor
		}

		public Order(String name) {
			this.name = name;
		}

		public Account getAccount() {
			return account;
		}
	}
}
