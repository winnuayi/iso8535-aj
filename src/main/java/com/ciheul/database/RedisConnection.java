package com.ciheul.database;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnection {
	private static JedisPool _pool = null;
	private static RedisConnection _singleton = null;

	private static final Logger logger = Logger.getLogger(RedisConnection.class);

	/** A private Constructor prevents any other class from instantiating. */
	private RedisConnection() {
		logger.debug("Begin to create Jedis Pool Configuration");
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setBlockWhenExhausted(poolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED);
		poolConfig.setJmxEnabled(poolConfig.DEFAULT_JMX_ENABLE);
		poolConfig.setMaxIdle(poolConfig.DEFAULT_MAX_IDLE);
		poolConfig.setMaxTotal(poolConfig.DEFAULT_MAX_TOTAL);
		poolConfig.setMaxWaitMillis(poolConfig.DEFAULT_MAX_WAIT_MILLIS);
		poolConfig.setMinEvictableIdleTimeMillis(poolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
		poolConfig.setMinIdle(poolConfig.DEFAULT_MIN_IDLE);
		poolConfig.setNumTestsPerEvictionRun(poolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN);
		poolConfig.setSoftMinEvictableIdleTimeMillis(poolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
		poolConfig.setTimeBetweenEvictionRunsMillis(poolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);

		_pool = new JedisPool(poolConfig, Context.REDIS_HOST, Context.REDIS_PORT);
		if (_pool != null) {
			logger.debug("Succes to create Jedis Pool");
		} else {
			logger.error("Failed to create Jedis Pool");
		}
	}

	public Jedis getConnection() {
		Jedis jedis = null;
		try {
			jedis = _pool.getResource();
		} catch (JedisConnectionException jce) {
			jce.printStackTrace();
		}
		return jedis;
	}

	public void closeConnection(Jedis jedis) {
		try {
			if (jedis != null) {
				_pool.returnResource(jedis);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeBrokenConnection(Jedis jedis) {
		try {
			if (jedis != null) {
				_pool.returnBrokenResource(jedis);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static RedisConnection getInstance() {
		if (_singleton == null) {
			_singleton = new RedisConnection();
		}
		return _singleton;
	}

}
