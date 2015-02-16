package com.ciheul.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class DBConnection {

	private static DBConnection _dbSingleton = null;
	private static ConnectionPool _pool = null;

	private boolean _flag = true;

	private static final Logger logger = Logger.getLogger(DBConnection.class);

	/** A private Constructor prevents any other class from instantiating. */
	private DBConnection() {
		logger.debug("Initiate creation of Database Connection Pool");
		Class<?> c = null;
		try {
			c = Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("Failed to find class driver : " + e.getMessage());
			_flag = false;
		}

		Driver driver = null;

		try {
			driver = (Driver) c.newInstance();
		} catch (InstantiationException e1) {
			logger.error("Failed to process DB Connection instance initiation : " + e1.getMessage());
			// _flag = false;
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			logger.error("Failed to process DB Connection instance initiation : " + e1.getMessage());
			_flag = false;
			e1.printStackTrace();
		}

		try {
			DriverManager.registerDriver(driver);
		} catch (SQLException e) {
			logger.error("Failed to register Database Driver : " + e.getMessage());
			_flag = false;
		}
		if (_flag) {
			PoolProperties poolProperties = new PoolProperties();
			// set pull properties

			String hostname = Context.DB_HOSTNAME;
			String dbName = Context.DB_NAME;
			StringBuilder connectionUrl = new StringBuilder(Context.DB_URL);
			connectionUrl.append(hostname);
			connectionUrl.append("/").append(dbName);
			poolProperties.setUrl(connectionUrl.toString());

			poolProperties.setDriverClassName(Context.DB_DRIVER);
			poolProperties.setUsername(Context.DB_USERNAME);
			poolProperties.setPassword(Context.DB_PASSWORD);
			poolProperties.setJmxEnabled(Context.DB_JMX_ENABLED);
			poolProperties.setTestWhileIdle(Context.DB_TEST_WHILE_IDLE);
			poolProperties.setTestOnBorrow(Context.DB_TEST_WHILE_IDLE);
			poolProperties.setValidationQuery(Context.DB_VALIDATION_QUERY);
			poolProperties.setTestOnReturn(Context.DB_TEST_ON_RETURN);
			poolProperties.setValidationInterval(Context.DB_VALIDATION_INTERVAL);
			poolProperties.setTimeBetweenEvictionRunsMillis(Context.DB_TIME_BETWEEN_EVICTION);
			poolProperties.setMaxActive(Context.DB_MAX_ACTIVE);
			poolProperties.setInitialSize(Context.DB_INITIAL_SIZE);
			poolProperties.setMaxWait(Context.DB_MAX_WAIT);
			poolProperties.setRemoveAbandonedTimeout(Context.DB_REMOVE_ABANDONED_TIMEOUT);
			poolProperties.setMinEvictableIdleTimeMillis(Context.DB_MIN_EVICTABLE_IDLE);
			poolProperties.setMinIdle(Context.DB_MIN_IDLE);
			poolProperties.setLogAbandoned(Context.DB_LOG_ABANDONED);
			poolProperties.setRemoveAbandoned(Context.DB_REMOVE_ABANDONED);
			poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
					+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
			try {
				_pool = new ConnectionPool(poolProperties);
				logger.debug("Database Connection Pool created");
			} catch (SQLException e) {
				logger.error("Failed to create Database Connection pool : " + e.getMessage());
				e.printStackTrace();
			}

		}
	}

	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = _pool.getConnection();
			_flag = true;
		} catch (SQLException e) {
			logger.error("Fail to create connection : " + e.getMessage());
			_flag = false;
		}
		return conn;
	}

	/** Static 'instance' method */
	public static DBConnection getInstance() {
		if (_dbSingleton == null) {
			_dbSingleton = new DBConnection();
		}
		return _dbSingleton;
	}

	public boolean getConnectionStatus() {
		return _flag;
	}

}
