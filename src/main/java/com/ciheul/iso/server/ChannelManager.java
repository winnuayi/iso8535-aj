package com.ciheul.iso.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
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
	private static ChannelManager _cMSingleTon = null;
	private long MAX_TIME_OUT;
	private AJMUX mux;
	private LocalSpace sp;
	private String in;
	private String out;

	private RedisConnection r = RedisConnection.getInstance();

	public static void logISOMsg(ISOMsg msg) {
		System.out.println("----ISO MESSAGE-----");
		try {
			System.out.println("  MTI : " + msg.getMTI());
			for (int i = 1; i <= msg.getMaxField(); i++) {
				if (msg.hasField(i)) {
					System.out.println("    Field-" + i + " : " + msg.getString(i));
				}
			}
		} catch (ISOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("--------------------");
		}
	}

	@Override
	protected void initService() throws ISOException {
		log.info("initializing ChannelManager Service");
		try {
			mux = (AJMUX) NameRegistrar.get("mux." + cfg.get("mux"));

			in = mux.getInQueue();
			out = mux.getOutQueue();

			sp = (LocalSpace) mux.getSpace();
			sp.addListener(in, this);

			MAX_TIME_OUT = cfg.getLong("timeout");
			NameRegistrar.register("manager", this);
		} catch (NameRegistrar.NotFoundException e) {
			log.error("Error in initializing service :" + e.getMessage());
		}
	}

	protected void startService() throws ISOException {
		System.out.println("cekkk");

		Timer timer = new Timer();

		timer.schedule(new TimerTask() {
			public void run() {
				// do your work
				System.out.println(mux.isConnected());

				Map<String, String> reversal = DatabaseManager.getReversal();
				if (mux.isConnected() && !reversal.toString().equals("{}")) {
					sendLinkUp(reversal);
				}
			}
		}, 0, Context.LINK_UP_THREAD_TIME);

		// Runnable linkUp = new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// System.out.println(mux.isConnected());
		// if (mux.isConnected()) {
		// sendLinkUp();
		// }
		// }
		// };
		// new Thread(linkUp).start();
	}

	/**
	 * Send Link up response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendLinkUp(Map<String, String> reversal) {

		System.out.println("masuk");

		String reversalString = reversal.values().toString();
		String[] reversalMessage = reversalString.substring(1, reversalString.length() - 1).split(",");

		int jumlah = 0;
		int tambah = 1;
		String revelsalMsgStr = "";
		int reversalSize = reversalMessage.length;
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
				System.out.println("\nsendLinkUp");
				try {

					Map<String, String> date = getDate();
					ISOMsg msg = new ISOMsg();

					msg.setMTI("0400");
					System.out.println();
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
					System.out.println("request : " + new String(messageBody));
					ChannelManager.logISOMsg(msg);

					int count = 0;
					ISOMsg reply = null;
					String replyStr = "";

					while (count < 4 && (reply == null || replyStr.equals(""))) {
						count = count + 1;
						if (reply != null) {
							if (reply.getValue(39).equals("00")) {
								replyStr = "success";
							} else {
								reply = sendMsg(msg);
							}
						} else {
							System.out.println("masuk sini");
							reply = sendMsg(msg);
						}
					}
					if (reply != null) {
						if (reply.getValue(39).equals("00")) {
							System.out.println("masuk sini juga");
							if (msg.getValue(48).toString().substring(0, 4).equals("2112")) {

								System.out.println(msg.getValue(48).toString().substring(4, 16));
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 16));
							}else if (msg.getValue(48).toString().substring(0, 4).equals("2114")) {

								System.out.println("masuk sini juga B");
								DatabaseManager.DelReversal(msg.getValue(48).toString().substring(4, 17));
							}
						}
					}
					// source.send(msg);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	private static HashMap<String, String> getDate() {
		DateFormat dateFormat1 = new SimpleDateFormat("MMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("HH");
		DateFormat dateFormat3 = new SimpleDateFormat("mmss");

		TimeZone timeZone = TimeZone.getTimeZone("GMT+07");
		dateFormat1.setTimeZone(timeZone);
		dateFormat2.setTimeZone(timeZone);
		dateFormat3.setTimeZone(timeZone);

		Date newDate = new Date();
		String bit7 = dateFormat1.format(newDate) + dateFormat2.format(newDate) + dateFormat3.format(newDate);
		String bit12 = dateFormat2.format(newDate) + dateFormat3.format(newDate);
		String bit13 = dateFormat1.format(newDate);
		String bit15 = Integer.toString(Integer.parseInt(bit13) + 1);

		HashMap<String, String> result = new HashMap<String, String>();

		result.put("bit7", bit7);
		result.put("bit12", bit12);
		result.put("bit13", bit13);
		result.put("bit15", bit15);

		return result;
	}

	public ISOMsg sendMsg(ISOMsg m) throws Exception {
		return sendMsg(m, mux, MAX_TIME_OUT);
	}

	private ISOMsg sendMsg(ISOMsg m, AJMUX mux, long timeout) throws Exception {
		System.out.println("mux sending message...");
		long start = System.currentTimeMillis();

		ISOMsg resp = null;

		// if connection is not established, LINK DOWN
		if (mux.isConnected() == false) {
			System.out.println("masuk");
			resp = (ISOMsg) m.clone();
			resp.set(39, "404");
			if (Integer.parseInt(m.getValue(4).toString()) > 0
					&& !m.getValue(48).toString().substring(0, 4).equals("2111")) {

				sendLinkUp(m);
			}
			return resp;
		}

		// append message to 'out' queue
		// sp.out(out, m, timeout);
		DatabaseManager.setStan(m.getValue(11).toString());
		Object obj = mux.request(m, timeout);

		// wait till exists and take away message from 'in' queue
		// Object obj = sp.in(in, timeout);

		// success to receive response message from server
		if (obj instanceof ISOMsg) {
			resp = (ISOMsg) obj;
			DatabaseManager.deleteStan(m.getValue(11).toString());

			logISOMsg(resp);

			// LINK DOWN
			if (resp.getValue(39).toString().equals("404")) {
				System.out.println("masuk");
				if (Long.parseLong(m.getValue(4).toString()) > 0
						&& !m.getValue(48).toString().substring(0, 4).equals("2111")) {

					sendLinkUp(m);
				}
				m.set(39, "404");
				return m;
			}

			return resp;
		}

		// timeout
		if (obj == null) {
			System.out.println("TIMEOUT BRO");
			resp = (ISOMsg) m.clone();
			resp.set(39, "68");
			return resp;
		}

		long duration = System.currentTimeMillis() - start;
		log.info("Response time (ms):" + duration);
		return resp;
	}

	public void sendLinkUp(ISOMsg m) {

		String billNumber = "";
		try {
			if (m.getValue(48).toString().substring(0, 4).equals("2112")) {

				billNumber = m.getValue(48).toString().substring(4, 16);
			} else if (m.getValue(48).toString().substring(0, 4).equals("2114")) {

				billNumber = m.getValue(48).toString().substring(4, 17);
			}
			String reversalMessage = m.getValue(4).toString() + "#" + m.getValue(7).toString() + "#"
					+ m.getValue(11).toString() + "#" + m.getValue(37).toString() + "#" + m.getValue(42).toString()
					+ "#" + m.getValue(48).toString() + "#" + "0200" + m.getValue(11).toString()
					+ m.getValue(7).toString() + m.getValue(32).toString() + "00000000000" + "#" + billNumber;
			System.out.println("masuk :" + reversalMessage);
			DatabaseManager.setReversal(billNumber, reversalMessage);
		} catch (NumberFormatException | ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ChannelManager getInstance() {
		if (_cMSingleTon == null) {
			System.out.println("*************");
			System.out.println("channel manager is null");
			System.out.println("*************");
			// _cMSingleTon = new ChannelManager();
			try {
				_cMSingleTon = ((ChannelManager) NameRegistrar.get("manager"));
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("*************");
			System.out.println("channel manager is not null");
			System.out.println("*************");
		}
		return _cMSingleTon;
	}

	@Override
	public void notify(Object key, Object value) {
		System.out.println("*************");
		System.out.println("channelmanager.notify()");
		System.out.println("*************");
	}
}