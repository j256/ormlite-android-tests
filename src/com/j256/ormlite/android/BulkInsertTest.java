package com.j256.ormlite.android;

import java.sql.Savepoint;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Inserting lots of data tests.
 * 
 * @author graywatson
 */
public class BulkInsertTest extends BaseDaoTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void testBatch() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);

		logger.info("starting bulk no-batch run");

		long before = System.currentTimeMillis();
		doInserts(dao);
		long noBatchTimeMs = System.currentTimeMillis() - before;
		logger.info("bulk no-batch run finished after {}ms", noBatchTimeMs);

		logger.info("starting bulk batch run");
		before = System.currentTimeMillis();
		dao.callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {
				doInserts(dao);
				return null;
			}
		});
		long batchTimeMs = System.currentTimeMillis() - before;
		logger.info("bulk batch run finished after {}ms", batchTimeMs);
		assertTrue(batchTimeMs < noBatchTimeMs);
	}

	public void testAutoCommit() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);

		logger.info("starting autocommit(true) no-batch run");
		long before = System.currentTimeMillis();
		doInserts(dao);
		long noBatchTimeMs = System.currentTimeMillis() - before;
		logger.info("bulk autocommit(true) run finished after {}ms", noBatchTimeMs);

		logger.info("starting autocommit(false) batch run");
		before = System.currentTimeMillis();
		DatabaseConnection conn = dao.startThreadConnection();
		try {
			conn.setAutoCommit(false);
			doInserts(dao);
		} finally {
			conn.setAutoCommit(true);
			dao.endThreadConnection(conn);
		}
		long batchTimeMs = System.currentTimeMillis() - before;
		logger.info("bulk autocommit(false) run finished after {}ms", batchTimeMs);
		assertTrue(batchTimeMs < noBatchTimeMs);
	}

	public void testInsertInBatches() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		logger.info("starting batch run using transactions directly");
		long before = System.currentTimeMillis();
		DatabaseConnection conn = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {
			savePoint = conn.setSavePoint(null);
			for (int i = 0; i < 10000; i++) {
				Foo foo = new Foo();
				foo.stuff1 = Integer.toString(i);
				assertEquals(1, dao.create(foo));
				// every so often commit the transaction and then start the next one
				if (i % 1000 == 0) {
					conn.commit(savePoint);
					savePoint = conn.setSavePoint(null);
				}
			}
		} finally {
			// commit at the end
			conn.commit(savePoint);
			dao.endThreadConnection(conn);
		}
		long noBatchTimeMs = System.currentTimeMillis() - before;
		logger.info("bulk transaction run finished after {}ms", noBatchTimeMs);
	}

	private void doInserts(final Dao<Foo, Integer> dao) throws Exception {
		for (int i = 0; i < 1000; i++) {
			Foo foo = new Foo();
			foo.stuff1 = Integer.toString(i);
			assertEquals(1, dao.create(foo));
		}
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff1;
		@DatabaseField
		String stuff2;
		@DatabaseField
		String stuff3;
		@DatabaseField
		String stuff4;
		@DatabaseField
		String stuff5;
		@DatabaseField
		String stuff6;
		@DatabaseField
		String stuff7;
		@DatabaseField
		String stuff8;
		@DatabaseField
		String stuff9;
		@DatabaseField
		String stuff10;
		public Foo() {
		}
	}
}
