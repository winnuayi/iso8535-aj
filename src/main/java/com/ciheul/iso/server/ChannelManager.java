package com.ciheul.iso.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

import com.ciheul.database.Context;
import com.ciheul.database.DatabaseManager;
import com.ciheul.database.RedisConnection;
import com.ciheul.iso.AJMUX;

public class ChannelManager extends QBeanSupport implements SpaceListener {
	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private static ChannelManager _cMSingleTon = null;
	private long MAX_TIME_OUT;
	private AJMUX mux;
	private LocalSpace sp;
	private String in;
	private String out;
	IsoEchoTest echoTest = new IsoEchoTest();

	private RedisConnection r = RedisConnection.getInstance();

	public static void logISOMsg(ISOMsg msg) {
		// logger.info("----ISO MESSAGE-----");

		try {
			String mti = String.format("[0:%s]", msg.getMTI());
			StringBuilder isoMsg = new StringBuilder(mti);
			// logger.info(" MTI : " + msg.getMTI());
			for (int i = 1; i <= msg.getMaxField(); i++) {
				if (msg.hasField(i)) {
					// logger.info(" Field-" + i + " : " + msg.getString(i));
					String field = String.format("[%d:%s]", i, msg.getString(i));
					isoMsg.append(field);
				}
			}
			logger.info(isoMsg);
		} catch (ISOException e) {
			e.printStackTrace();
		} finally {
			// logger.info("--------------------");
		}
	}

	@Override
	protected void initService() throws ISOException {
//		logger.info("initializing ChannelManager Service");
		try {
			mux = (AJMUX) NameRegistrar.get("mux." + cfg.get("mux"));

			in = mux.getInQueue();
			out = mux.getOutQueue();

			// System.out.println("cfg: " + cfg.get("mux"));
			// System.out.println("mux: " + mux.toString());
			// System.out.println("in : " + in);
			// System.out.println("out: " + out);

			sp = (LocalSpace) mux.getSpace();
			sp.addListener(in, this);

			MAX_TIME_OUT = cfg.getLong("timeout");

			// TODO BAD PRACTICE!!!
			if (cfg.get("mux").equals("jpos-client-mux")) {
				NameRegistrar.register("manager", this);
			} else {
				NameRegistrar.register("aj-postpaid-ntl-manager", this);
			}

		} catch (NameRegistrar.NotFoundException e) {
			logger.error("Error in initializing service :" + e.getMessage());
		}
	}

	private void sendEchoTest() {
		String stanId = "000001";
		Map<String, String> date = getDate();
		// ISOMsg reply = null;

		try {
			ISOMsg msg = new ISOMsg();

			msg.setMTI("0800");
			msg.set(7, date.get("bit7"));
			msg.set(11, stanId);
			msg.set(70, "301");
			msg.setPackager(new ISO87APackager());

			byte[] messageBody = msg.pack();

			logISOMsg(msg);

			try {
				sendMsg(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (ISOException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Send sign-on response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendSignOnRequest(ISOMsg m) {
		ISOMsg reply = null;
		try {
			ISOMsg msg = (ISOMsg) m.clone();
			msg.setMTI("0800");
			msg.set(7, ISODate.getDateTime(new Date()));
			msg.set(11, "000002");
			msg.set(70, "001");
			msg.setPackager(new ISO87APackager());
			byte[] messageBody = msg.pack();

			ChannelManager.logISOMsg(msg);
			try {
				reply = sendMsg(msg);
				if (reply != null) {
					if (reply.getValue(39).equals("00")) {
						DatabaseManager.setIsConnected("true");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (ISOException e) {
			logger.error(e.getMessage());
		}
	}

	protected void startService() throws ISOException {
		final ISOMsg m = new ISOMsg();

		// switching server demands the Q2 to always send echo message periodically
		// and if it has been not connected, send sign on request message
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				if (mux.isConnected()) {
					sendEchoTest();
					if (!DatabaseManager.getIsConnected().equals("true")) {
						sendSignOnRequest(m);
					}
				}
			}
		}, 0, Context.ECHO_TEST_TIME);

		// periodically check if connected or linked up after disconnect,
		// when true, send reversal message that has been stored in redis
		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			public void run() {
				Map<String, String> reversal = DatabaseManager.getReversal();

				// for (int i = 0; i < 100; i++) {
				// String key = "jpos-client-send" + "." + "0200" + String.format("%05d", i);
				// System.out.println("ChannelManager.startService; key: " + key);
				// Object o = sp.rd(key, 100);
				// System.out.println(o);
				//
				// String key2 = "jpos-client-receive" + "." + "0200" + String.format("%05d", i);
				// System.out.println("ChannelManager.startService; key2: " + key2);
				// Object o2 = sp.rd(key2, 100);
				// System.out.println(o2);
				// }

				// System.out.println("================");
				// System.out.println("DB isconnected : " + DatabaseManager.getIsConnected());
				// System.out.println("MUX isConnected : " + mux.isConnected());
				// System.out.println("Reversal SAF : " + reversal.toString());

				if (mux.isConnected() && !reversal.toString().equals("{}")
						&& DatabaseManager.getIsConnected().equals("true")) {
					sendLinkUp(reversal);
				} else {
					// System.out.println("No Reversal in SAF");

				}
			}
		}, 0, Context.LINK_UP_THREAD_TIME);
	}

	/**
	 * This method name is confusing with another method that has exactly the same name but different parameters It does
	 * not send message to switching server, but store reversal message to redis.
	 * 
	 * @param m
	 */
	public void sendLinkUp(ISOMsg m) {
		String billNumber = "";
		try {
			// get bill number from bit 48, either postpaid or NTL
			if (m.getValue(48).toString().substring(0, 4).equals("2112")) {
				billNumber = m.getValue(48).toString().substring(4, 16);
			} else if (m.getValue(48).toString().substring(0, 4).equals("2114")) {
				billNumber = m.getValue(48).toString().substring(4, 17);
			}

			// build reversal message
			String reversalMessage = m.getValue(4).toString() + "#" + m.getValue(7).toString() + "#"
					+ m.getValue(11).toString() + "#" + m.getValue(37).toString() + "#" + m.getValue(42).toString()
					+ "#" + m.getValue(48).toString() + "#" + "0200" + m.getValue(11).toString()
					+ m.getValue(7).toString() + m.getValue(32).toString() + "00000000000" + "#" + billNumber;

			System.out.println("masuk :" + reversalMessage);

			// store reversal message to redis
			DatabaseManager.setReversal(billNumber, reversalMessage);
		} catch (NumberFormatException | ISOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send revesal message immediately when connection has been linked up.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendLinkUp(Map<String, String> reversal) {
		String reversalString = reversal.values().toString();
		String[] reversalMessage = reversalString.substring(1, reversalString.length() - 1).split(",");

		int jumlah = 0;
		int tambah = 1;
		String revelsalMsgStr = "";
		// int reversalSize = reversalMessage.length; // never used locally

		for (int i = 0; i < reversalMessage.length; i += tambah) {
			revelsalMsgStr = reversalMessage[i];
			if (!revelsalMsgStr.equals("")) {
				tambah = 1;
				String[] reversalMsg = reversalMessage[i].split("#");
				jumlah = jumlah + reversalMsg.length;

				while (jumlah < 8) {
					revelsalMsgStr += "," + reversalMessage[i + tambah];
					reversalMsg = reversalMessage[i + tambah].split("#");

					jumlah = jumlah + reversalMsg.length - 1;
					tambah++;
				}

				String[] reversalMsgSent = revelsalMsgStr.split("#");

				try {
					Map<String, String> date = getDate();
					ISOMsg msg = new ISOMsg();

					msg.setMTI("0400");
					// System.out.println();
					// msg.set(1, "723A400128618002");
					msg.set(2, Context.ISO_BIT2);
					msg.set(3, Context.ISO_BIT3_PAY);
					msg.set(4, reversalMsgSent[0]);
					msg.set(7, reversalMsgSent[1]);
					msg.set(11, reversalMsgSent[2]); // postpaid
					// msg.set(11, "890931"); // prepaid
					msg.set(12, date.get("bit12"));
					msg.set(13, date.get("bit13"));
					msg.set(15, date.get("bit15"));
					msg.set(18, Context.ISO_BIT18);
					msg.set(32, Context.ISO_BIT32);
					msg.set(35, Context.ISO_BIT35);
					msg.set(37, reversalMsgSent[3]);
					msg.set(42, reversalMsgSent[4]);
					msg.set(43, Context.ISO_BIT43);
					msg.set(48, reversalMsgSent[5]);
					msg.set(49, "360");
					msg.set(63, "214");
					msg.set(90, reversalMsgSent[6]);
					msg.setPackager(new ISO87APackager());

					byte[] messageBody = msg.pack();
					// System.out.println("request : " + new String(messageBody));
					ChannelManager.logISOMsg(msg);

					int count = 0;
					ISOMsg reply = null;
					String replyStr = "";

					while (count < 3 && (reply == null || replyStr.equals(""))) {
						logger.info("request : " + new String(messageBody));

						count = count + 1;

						if (reply != null) {
							if (reply.getValue(39).equals("00") || reply.getValue(39).equals("94")
									|| reply.getValue(39).equals("63")) {
								logger.info("Link up response: " + reply.pack());
								replyStr = "success";
							} else {
								reply = sendMsg(msg);
							}
						} else {
							System.out.println("masuk sini");
							reply = sendMsg(msg);
						}

						if (count == 3) {
							if (msg.getValue(48).toString().substring(0, 4).equals("2112")) {
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 16));
							} else if (msg.getValue(48).toString().substring(0, 4).equals("2114")) {
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 17));
							}
						}
					}

					// if there exists reply and rc is 00 (success), 63 (), or 94 ()
					// remove reversal message from redis
					if (reply != null) {
						if (reply.getValue(39).equals("00") || reply.getValue(39).equals("94")
								|| reply.getValue(39).equals("63")) {
							if (msg.getValue(48).toString().substring(0, 4).equals("2112")) {
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 16));
							} else if (msg.getValue(48).toString().substring(0, 4).equals("2114")) {
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 17));
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public ISOMsg sendMsg(ISOMsg m) throws Exception {
		return sendMsg(m, mux, MAX_TIME_OUT);
	}

	private ISOMsg sendMsg(ISOMsg m, AJMUX mux, long timeout) throws Exception {
		long start = System.currentTimeMillis();

		ISOMsg resp = null;

		// if connection between Q2 and switching server is not established, LINK DOWN
		if (mux.isConnected() == false) {
			log.info("Link Down before sending.");
			// set result code to 404 which means link down
			resp = (ISOMsg) m.clone();
			resp.set(39, "404");

			// if m is a reversal message (and of course it must be postpaid or NTL),
			// store reversal message to redis (later, when it is linked up, send reversal automatically)
			if (Long.parseLong(m.getValue(4).toString()) > 0 && m.getMTI().equals("0400")
					&& !m.getValue(48).toString().substring(0, 4).equals("2111")) {
				sendLinkUp(m);
			}

			return resp;
		}

		// append message to 'out' queue
		// sp.out(out, m, timeout);

		// set STAN to redis
		DatabaseManager.setStan(m.getValue(11).toString());

		System.out.println("======== BEFORE SENDING =======");
		// logISOMsg(m);
		// HERE YOU GO, LADS! send request message to switching server
		Object obj = mux.request(m, timeout);

		// wait till exists and take away message from 'in' queue
		// Object obj = sp.in(in, timeout);

		// success to receive response message from server
		// System.out.println("object : " + obj);
		// System.out.println("message : " + m);
		// System.out.println(timeout);

		// connection to switching fails due to request timeout
		if (obj == null) {
			resp = (ISOMsg) m.clone();
			resp.set(39, "68");
			return resp;
		}

		if (obj instanceof ISOMsg) {
			resp = (ISOMsg) obj;

			DatabaseManager.deleteStan(m.getValue(11).toString());

			System.out.println("======== AFTER RECEIVING =======");
//			logISOMsg(resp);

			// LINK DOWN
			if (resp.getValue(39).toString().equals("404")) {
				log.info("Link Down after sending.");

				// for postpaid or NTL, when link down,
				// store reversal message to redis (later, when it is linked up, send reversal automatically)
				if (Long.parseLong(m.getValue(4).toString()) > 0
						// && m.getMTI().equals("0400")
						&& !m.getValue(48).toString().substring(0, 4).equals("2111")) {
					sendLinkUp(m);
				}

				m.set(39, "404");

				return m;
			}

			long duration = System.currentTimeMillis() - start;
			log.info("Response time (ms):" + duration);

			return resp;
		}

		// TODO check whether this line will be reached. if it will not, then remove these following lines
		long duration = System.currentTimeMillis() - start;
		log.info("Response time (ms):" + duration);

		return resp;
	}

	private static HashMap<String, String> getDate() {
		DateFormat dateFormat1 = new SimpleDateFormat("MMdd");
		DateFormat dateFormat2Bit7 = new SimpleDateFormat("HH");
		DateFormat dateFormat2Bit12 = new SimpleDateFormat("HH");
		DateFormat dateFormat3 = new SimpleDateFormat("mmss");

		TimeZone timeZone = TimeZone.getTimeZone("GMT+00");
		TimeZone timeZone2 = TimeZone.getTimeZone("GMT+07");

		dateFormat1.setTimeZone(timeZone);
		dateFormat2Bit7.setTimeZone(timeZone); // CR#3
		dateFormat2Bit12.setTimeZone(timeZone2); // CR#3
		dateFormat3.setTimeZone(timeZone);

		Date newDate = new Date();
		String bit7 = dateFormat1.format(newDate) + dateFormat2Bit7.format(newDate) + dateFormat3.format(newDate);
		String bit12 = dateFormat2Bit12.format(newDate) + dateFormat3.format(newDate);
		String bit13 = dateFormat1.format(newDate);
		String bit15 = Integer.toString(Integer.parseInt(bit13) + 1);

		HashMap<String, String> result = new HashMap<String, String>();

		result.put("bit7", bit7);
		result.put("bit12", bit12);
		result.put("bit13", bit13);
		result.put("bit15", bit15);

		return result;
	}

	public static ChannelManager getInstance() {
		if (_cMSingleTon == null) {
			// System.out.println("*************");
			// System.out.println("channel manager is null");
			// System.out.println("*************");
			// _cMSingleTon = new ChannelManager();

			try {
				_cMSingleTon = ((ChannelManager) NameRegistrar.get("manager"));
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		} else {
			// System.out.println("*************");
			// System.out.println("channel manager is not null");
			// System.out.println("*************");
		}

		return _cMSingleTon;
	}

	@Override
	public void notify(Object key, Object value) {
		// System.out.println("*************");
		// System.out.println("channelmanager.notify()");
		// System.out.println("*************");
	}
}