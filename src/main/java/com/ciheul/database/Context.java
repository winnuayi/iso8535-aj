package com.ciheul.database;

public class Context {

	public static final int LINK_UP_THREAD_TIME = 10000;
	public static final int ECHO_TEST_TIME = 30000;
	public static final int PENDING_STATUS = 2;
	public static final int SUCCESS_STATUS = 3;
	public static final int FAIL_STATUS = 1;
	
	// POOL DATABASE CONFIGURATION
	public static final String DB_USERNAME = "ciheul";
	public static final String DB_PASSWORD = "";
	public static final String DB_NAME = "axes-live";
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
	public static final String REVERSAL_MESSAGE = "reversalMessage";
	public static final String TYPE_EXCLUDED = "EX";
	public static final String TYPE_INCLUDED = "IN";
    public static final String STAN = "stan";
    
    // ISO BIT VARIABLE
	public static final String ISO_BIT18 = "6021";
	public static final String ISO_BIT42 = "AXS9999        ";
	public static final String ISO_BIT49 = "360";
	public static final String ISO_BIT2 = "454633334444";
	public static final String ISO_BIT3_INQ = "380000";
	public static final String ISO_BIT3_PAY = "180000";
	public static final String ISO_BIT32 = "000735";
	public static final String ISO_BIT43 = "AXES                                    ";
	public static final String ISO_BIT48_NONTAGLIST = "2114";
	public static final String ISO_BIT48_NONTAGLIST_TAIL = "000";
	public static final String ISO_BIT48_POSPAID = "2112";
	public static final String ISO_BIT48_PREPAID = "2111";
	public static final String ISO_BIT63 = "214";
	public static final String ISO_BIT35 = "454633334444=;=0909";
}
