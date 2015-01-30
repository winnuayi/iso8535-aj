package com.ciheul.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import org.apache.log4j.Logger;



import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.ciheul.database.Context;
import com.ciheul.database.DBConnection;
import com.ciheul.database.DatabaseManager;

public class DatabaseManager {

//	private static final Logger logger = Logger
//			.getLogger(DatabaseManager.class);

	public static void updateBit48(String billNumber1, String billNumber2,
			String transactionId, String bit48, int status) {

		PreparedStatement prepStatement = null;
		DBConnection dbConn = null;
		Connection conn = null;
		ResultSet rs = null;

		String updatebit48Transaction = " Update adm_transaction set bit_48=? where transaction_id=? AND (bill_number=? or bill_number=?) AND status=?";

		try {
			dbConn = DBConnection.getInstance();
			conn = dbConn.getConnection();
			conn.setAutoCommit(false);

			prepStatement = conn.prepareStatement(updatebit48Transaction);
			prepStatement.setString(1, bit48);
			prepStatement.setString(2, transactionId);
			prepStatement.setString(3, billNumber1);
			prepStatement.setString(4, billNumber2);
			prepStatement.setInt(5, status);
//			logger.info("Query : " + prepStatement.toString());

			int statuss = prepStatement.executeUpdate();
			if (statuss == 1) {
//				logger.info("Query success ");
			} else {
//				logger.info("Query failed ");
			}

			if (statuss != 1) { // fail
//				logger.warn("Failed to update bit48 on transaction "
//						+ transactionId);
				conn.rollback();
			}
			conn.commit();
			
		} catch (SQLException e) {
//			logger.error("Error on update bit 48 on transaction : " + e.getMessage());
			e.printStackTrace();

			if (conn != null) {
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}

		} catch (Exception e) {
//			logger.error("Error on update bit 48 on transaction : " + e.getMessage());
			e.printStackTrace();
		} finally {
			
			if (prepStatement != null) {
				try {
					prepStatement.close();
				} catch (SQLException e) {
//					logger.error("Failed to close preparation Statement : "
//							+ e.getMessage());
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}

		}
	}
	/**
	 * Updating status on Transaction
	 * 
	 * @param transactionId
	 *            transactionId to be changed
	 * @param status
	 *            new status value
	 * @return
	 */
	public static boolean updateStatusTransaction(String transactionId,
			int status, String resultCode, String message) {
//		logger.debug("Begin updateStatusTransaction Method");
		boolean result = false;
		PreparedStatement prepStatement = null;
		DBConnection dbConn = null;
		Connection conn = null;

		String updateStatusQ = "UPDATE adm_transaction "
				+ "SET status=?, result_code=?, note=? WHERE transaction_id=? "
				+ "AND status=?";

		String revertBalanceQ = "INSERT INTO adm_mutasibalance  (created_user_by, transaction_id, "
				+ "product, date_created, transaction_type, customer_id, debit, credit, "
				+ "note, account_by_id, account_to_id, balance, transaction_status) (SELECT "
				+ "created_user_by, transaction_id, product, date_created, transaction_type, customer_id, "
				+ "credit, debit, 'Revert balance.', account_by_id, account_to_id, balance+debit, transaction_status "
				+ "FROM adm_mutasibalance WHERE" + " transaction_id=?)";

		String updateBalanceQ = "UPDATE adm_account SET balance=balance+subquery.amount "
				+ "FROM (SELECT amount, account_id FROM adm_transaction WHERE transaction_id=?) AS subquery "
				+ "WHERE id=subquery.account_id";

		try {
			dbConn = DBConnection.getInstance();
			conn = dbConn.getConnection();

			conn.setAutoCommit(false);

			prepStatement = conn.prepareStatement(updateStatusQ);
			prepStatement.setInt(1, status);
			prepStatement.setString(2, resultCode);
			prepStatement.setString(3, message);
			prepStatement.setString(4, transactionId);
			prepStatement.setInt(5, Context.PENDING_STATUS);
//			logger.info("Query : " + prepStatement.toString());
			int _result = prepStatement.executeUpdate();
			if (_result == 1) {
//				logger.info("Query success ");
			} else {
//				logger.info("Query failed ");
			}
			if (_result == 1) {
				result = true;
			}

			// in case of fail, return client's money
			if (Context.FAIL_STATUS == status && result) {
				prepStatement = conn.prepareStatement(revertBalanceQ);
				prepStatement.setString(1, transactionId);
//				logger.info("Query : " + prepStatement.toString());
				status = prepStatement.executeUpdate();
				if (status == 1) {
//					logger.info("Query success ");
				} else {
//					logger.info("Query failed ");
				}

				prepStatement = conn.prepareStatement(updateBalanceQ);
				prepStatement.setString(1, transactionId);
//				logger.info("Query : " + prepStatement.toString());
				status = prepStatement.executeUpdate();
				if (status == 1) {
//					logger.info("Query success ");
				} else {
//					logger.info("Query failed ");
				}
			}

			conn.commit();

		} catch (SQLException e) {
//			logger.error("Error on updating status of transaction : "
//					+ e.getMessage());
			e.printStackTrace();

			if (conn != null) {
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}

		} catch (Exception e) {
//			logger.error("Error on updating status of transaction : "
//					+ e.getMessage());
			e.printStackTrace();
		} finally {
			if (prepStatement != null) {
				try {
					prepStatement.close();
				} catch (SQLException e) {
//					logger.error("Failed to close preparation statement : "
//							+ e.getMessage());
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}
		}
//		logger.debug("Finish updateStatusTransaction Method");
		return result;
	}
	public static boolean insertTransaction(String transactionId,
			String transactionRef, int accountId, int productId,
			String billNumber, int status, String amount, String feeAdmin,
			String resultCode, String note, String bit61, String bit48,
			String date, String feeType) {
//		logger.debug("Begin insert Transaction Method");
		boolean result = false;
		PreparedStatement prepStatement = null;
		DBConnection dbConn = null;
		Connection conn = null;

		String transactionQ = "INSERT INTO adm_transaction "
				+ "(transaction_id, transaction_ref_id, account_id, "
				+ "product_id, bill_number, status, amount, result_code, "
				+ "note, bit_61, bit_48, timestamp) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			dbConn = DBConnection.getInstance();
			conn = dbConn.getConnection();

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"dd-MM-yyyy HH:mm:ss:FF");
			String msString = date.substring(date.lastIndexOf(":") + 1);
			double ms = Double.parseDouble(msString) / 1000;
			Date _date = simpleDateFormat.parse(date);
			long milis = _date.getTime() + (long) ms;

			BigDecimal _amount = new BigDecimal(0);

			if (amount != null && !amount.equals("")) {
				_amount = new BigDecimal(amount);
			}

			BigDecimal _feeAdmin = new BigDecimal(0);
			if (feeAdmin != null && !feeAdmin.equals("")) {
				_feeAdmin = new BigDecimal(feeAdmin);
			}
			if (feeType.equals(Context.TYPE_EXCLUDED)) {
				_amount = _amount.add(_feeAdmin);
			}

			prepStatement = conn.prepareStatement(transactionQ);
			prepStatement.setString(1, transactionId); // transaction_id
			prepStatement.setString(2, transactionRef); // transaction_ref_id
			prepStatement.setInt(3, accountId); // account_id
			prepStatement.setInt(4, productId); // product_id
			prepStatement.setString(5, billNumber);// bill_number
			prepStatement.setInt(6, status);// status
			prepStatement.setBigDecimal(7, _amount);// amount
			prepStatement.setInt(8, Integer.parseInt(resultCode));// result_code
			prepStatement.setString(9, note);// note
			prepStatement.setString(10, bit61);// bit_61
			prepStatement.setString(11, bit48);// bit_48
			prepStatement.setTimestamp(12, new java.sql.Timestamp(milis));// timestamp
//			logger.info("Query : " + prepStatement.toString());
			int _result = prepStatement.executeUpdate();
			if (_result == 1) {
//				logger.info("Query success ");
			} else {
//				logger.info("Query failed ");
			}

		} catch (SQLException e) {
//			logger.error("Error on updating status of transaction : "
//					+ e.getMessage());
			e.printStackTrace();

			if (conn != null) {
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}

		} catch (Exception e) {
//			logger.error("Error on updating status of transaction : "
//					+ e.getMessage());
			e.printStackTrace();
		} finally {
			if (prepStatement != null) {
				try {
					prepStatement.close();
				} catch (SQLException e) {
//					logger.error("Failed to close preparation statement : "
//							+ e.getMessage());
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
//					logger.error("Failed to close connection : "
//							+ se.getMessage());
				}
			}
		}
//		logger.debug("Finish insert transaction Method");
		return result;
	}

	/**
	 * Get advice
	 * 
	 * @return adviceMessage
	 */
	public static String getAdviceSuccess(String trxId) {
		Jedis jedis = null;
		RedisConnection rc = null;
		String result = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE_SUCCESS, trxId))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE_SUCCESS, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			result = jedis.hget(Context.ADVICE_MESSAGE_SUCCESS, trxId);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
		return result;
	}

	/**
	 * Set Advice
	 * 
	 * @return adviceMessage
	 */
	public static void setAdviceSuccess(String trxId, String msgBytes) {
		Jedis jedis = null;
		RedisConnection rc = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE_SUCCESS, trxId))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE_SUCCESS, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			jedis.hset(Context.ADVICE_MESSAGE_SUCCESS, trxId, msgBytes);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
	}


	/**
	 * Set Advice
	 * 
	 * @return adviceMessage
	 */
	public static void delAdviceSuccess(String trxId, String msgBytes) {
		Jedis jedis = null;
		RedisConnection rc = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE_SUCCESS, trxId))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE_SUCCESS, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			jedis.hdel(Context.ADVICE_MESSAGE_SUCCESS, trxId);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
	}

	/**
	 * Get advice
	 * 
	 * @return adviceMessage
	 */
	public static String getAdvice(String billNumber) {
		Jedis jedis = null;
		RedisConnection rc = null;
		String result = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE, billNumber))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			result = jedis.hget(Context.ADVICE_MESSAGE, billNumber);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
		return result;
	}

	/**
	 * Set Advice
	 * 
	 * @return adviceMessage
	 */
	public static void setAdvice(String billNumber, String msgBytes) {
		Jedis jedis = null;
		RedisConnection rc = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE, billNumber))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			jedis.hset(Context.ADVICE_MESSAGE, billNumber, msgBytes);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
	}


	/**
	 * Set Advice
	 * 
	 * @return adviceMessage
	 */
	public static void delAdvice(String billNumber) {
		Jedis jedis = null;
		RedisConnection rc = null;
		try {
			rc = RedisConnection.getInstance();
			jedis = rc.getConnection();

			if ((null == jedis.hget(Context.ADVICE_MESSAGE, billNumber))) {

//				logger.debug("ADVICE_MESSAGE is not listed on Redis yet. Begin to initiate Advice Message.");

				jedis.hset(Context.ADVICE_MESSAGE, "", "");
			}
			// result = jedis.incr(Context.IS_CONNECTED);
			jedis.hdel(Context.ADVICE_MESSAGE, billNumber);

		} catch (JedisConnectionException jce) {
//			logger.error("Error on redis connection : " + jce.getMessage());
			rc.closeBrokenConnection(jedis);
		} catch (Exception e) {
//			logger.error("Error on redis connection : " + e.getMessage());
		} finally {
			rc.closeConnection(jedis);
		}
	}
}
