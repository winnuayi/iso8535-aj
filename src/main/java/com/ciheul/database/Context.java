package com.ciheul.database;

public class Context {

	public static final int PENDING_STATUS = 2;
	public static final int SUCCESS_STATUS = 3;
	public static final int FAIL_STATUS = 1;
	
	// POOL DATABASE CONFIGURATION
	public static final String DB_USERNAME = "ciheul";
	public static final String DB_PASSWORD = "";
	public static final String DB_NAME = "axes-clone-live";
	public static final String DB_HOSTNAME = "";
	public static final String DB_URL = "jdbc:postgresql://";
	public static final String DB_DRIVER = "org.postgresql.Driver";
	public static final boolean DB_JMX_ENABLED = true;
	public static final boolean DB_TEST_WHILE_IDLE = false;
	public static final boolean DB_TEST_ON_BORROW = true;
	public static final String DB_VALIDATION_QUERY = "SELECT 1";
	public static final boolean DB_TEST_ON_RETURN = false;
	public static final int DB_VALIDATION_INTERVAL = 30000;
	public static final int DB_TIME_BETWEEN_EVICTION = 30000;
	public static final int DB_MAX_ACTIVE = 100;
	public static final int DB_INITIAL_SIZE = 10;
	public static final int DB_MAX_WAIT = 10000;
	public static final int DB_REMOVE_ABANDONED_TIMEOUT = 60;
	public static final int DB_MIN_EVICTABLE_IDLE = 30000;
	public static final int DB_MIN_IDLE = 10;
	public static final boolean DB_LOG_ABANDONED = true;
	public static final boolean DB_REMOVE_ABANDONED = true;


	// REDIS AND JEDIS CONFIGURATION
	public static final String REDIS_HOST = "localhost";
	public static final int REDIS_PORT = 6379;
	public static final String TRAX_ID_COUNTER = "traxIdCounter";
	public static final String STAN_PLN_COUNTER = "stanPLNCounter";
	public static final String IS_CONNECTED_PLN = "isConnectedPLN";
	public static final String REVERSAL_MESSAGE_SUCCESS = "reversalMessageSuccess";
	public static final String USER_REVERSAL_MESSAGE = "userReversalMessage";
	public static final String ADVICE_MESSAGE_SUCCESS = "adviceMessageSuccess";
	public static final String ADVICE_MESSAGE = "adviceMessage";
	public static final String TYPE_EXCLUDED = "EX";
	public static final String TYPE_INCLUDED = "IN";
    public static final String STAN = "stan";
}
