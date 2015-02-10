package com.ciheul.iso.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.packager.ISO87APackager;

import com.ciheul.database.Context;
import com.ciheul.database.DatabaseManager;

public class ClientRequestListener implements ISORequestListener {

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ChannelManager.class);
	private int LOGGEDIN = 1;
	private int NOT_LOGGEDIN = 0;

//	private static Logger logger = new Logger();
	private ChannelManager channelManager;
	private int state = NOT_LOGGEDIN;

	public ClientRequestListener() {
//		logger.addListener(new SimpleLogListener(System.out));
//		logger.debug(new SimpleLogListener(System.out));
	}

	/**
	 * MAIN HANDLER
	 */
	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		// <<<<<<< HEAD
		try {
			logger.info("incoming ISOMSG : "+m.getMTI());
		} catch (ISOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		channelManager.logISOMsg(m);
		channelManager = ChannelManager.getInstance();
		System.out.println("process start");

		// ChannelManager.logISOMsg(m);
		try {
			if (m.getMTI().equals("0800")) {
				// channelManager.sendMsg(createHandshakeISOMsg2());
				// System.out.println("late response ");
				sendEchoTestResponse(source, m);
//				sendLinkUp(source, m);
				// channelManager.sendMsg(createHandshakeISOMsg("0800", "001"));
				sendSignOnRequest(source, m);
			} else if (m.getMTI().equals("0810")) {
				// source.send(createHandshakeISOMsg2("0810", "001"));
				// channelManager.sendMsg(createHandshakeISOMsg("0810", "001"));
				// sendEchoTestResponse(source, m);
				// sendSignOnRequest2(source, m);
			} else if (m.getMTI().equals("0410")) {
				// source.send(createHandshakeISOMsg2("0810", "001"));
				// channelManager.sendMsg(createHandshakeISOMsg("0810", "001"));
				// sendEchoTestResponse(source, m);
				// sendSignOnRequest2(source, m);
				
			} else if (Integer.parseInt(m.getValue(4).toString()) > 0) {

				if (m.getMTI().equals("0210")) {
					if (m.getValue(48).toString().substring(0, 4).equals("2111")) {
						String adviceMessage1 = DatabaseManager.getAdvice(m.getValue(48).toString().substring(15, 27));
						String adviceMessage2 = DatabaseManager.getAdvice(m.getValue(48).toString().substring(4, 15));

						String rc = m.getValue(39).toString();
						if (m.getValue(39).toString().equals("68")) {
							rc = rc + "2";
						}
						if ((adviceMessage1==null || adviceMessage2==null)) {
							String msgBytes = m.getValue(4).toString()+"#"+rc+"#"+m.getValue(48).toString();
							DatabaseManager.setAdviceSuccess(""+Integer.parseInt(m.getValue(37).toString()), msgBytes);
						}else{
//							if (!m.getValue(39).toString().equals("68")) {

								DatabaseManager.updateBit48(m.getValue(48).toString().substring(4, 15), m.getValue(48)
										.toString().substring(15, 27), "" + Integer.parseInt(m.getValue(37).toString()), m
										.getValue(48).toString(), Context.PENDING_STATUS);
								if (m.getValue(39).toString().equals("00")) {
									DatabaseManager.updateStatusTransaction("" + m.getValue(37), Context.SUCCESS_STATUS, m
											.getValue(39).toString(), "Approved");
								} else {
									DatabaseManager.updateStatusTransaction("" + m.getValue(37), Context.FAIL_STATUS, rc, "Transaction Fail");
								}
								DatabaseManager.delAdvice(m.getValue(48).toString().substring(15, 27));
								DatabaseManager.delAdvice(m.getValue(48).toString().substring(4, 15));
//							}
						}
						// =======
						// System.out.println("=============");
						// System.out.println("process start");
						//
						// try {
						// if (m.getMTI().equals("0800")) {
						// sendEchoTestResponse(source, m);
						//
						// // do not send sign on request if system has been
						// logged in
						// if (state == NOT_LOGGEDIN) {
						// sendSignOnRequest(source, m);
						// }
						// } else if (m.getMTI().equals("0810")) {
						// if (m.getValue(70).equals("001")) {
						// state = LOGGEDIN;
						// }
						// } else if (Integer.parseInt(m.getValue(4).toString())
						// > 0) {
						// if (m.getMTI().equals("0210")) {
						// if (m.getValue(48).toString().substring(0,
						// 4).equals("2111")) {
						//
						// >>>>>>> e552205670449e869b3fcd9730fd84b325d8f03f
					} else {
						channelManager.sendMsg(createReversalISOMsg(m));
					}
				}
				// <<<<<<< HEAD
				// }else{
				//
				// System.out.println(m.getMTI());
				// channelManager.logISOMsg(m);
				// }
				// =======
			}
			// >>>>>>> e552205670449e869b3fcd9730fd84b325d8f03f
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("process end");
		System.out.println("===========");

		return false;
	}

	/**
	 * Send echo test response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendEchoTestRequest(ISOSource source, ISOMsg m) {
		System.out.println("\nsendEchoTestRequest");
		try {
			ISOMsg msg = (ISOMsg) m.clone();

			msg.setMTI("0800");
			msg.set(7, ISODate.getDateTime(new Date()));
			msg.set(11, "000002");
			msg.set(39, "00");
			msg.set(70, "301");
			msg.setPackager(new ISO87APackager());

			byte[] messageBody = msg.pack();
			System.out.println("request : " + new String(messageBody));

			source.send(msg);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send echo test response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendEchoTestResponse(ISOSource source, ISOMsg m) {
		System.out.println("\nsendEchoResponse");

		try {
			ISOMsg msg = (ISOMsg) m.clone();

			msg.setMTI("0810");
			msg.set(39, "00");
			// msg.set(70, "301");

			byte[] messageBody = msg.pack();
			System.out.println("request : " + new String(messageBody));
			ChannelManager.logISOMsg(msg);

			msg.setPackager(new ISO87APackager());
			source.send(msg);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Send Link up response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendLinkUp(ISOSource source, ISOMsg m) {

		Map<String, String> reversal = DatabaseManager.getReversal();
		
		String reversalString = reversal.values().toString();
		String[] reversalMessage = reversalString.substring(1,
				reversalString.length() - 1).split(",");
		
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
					ISOMsg msg = (ISOMsg) m.clone();

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
					
					byte[] messageBody = msg.pack();
					System.out.println(messageBody);
					System.out.println("request : " + new String(messageBody));
					ChannelManager.logISOMsg(msg);

					msg.setPackager(new ISO87APackager());
					source.send(msg);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

//		try {
//			ISOMsg msg = (ISOMsg) m.clone();
//
//			msg.setMTI("0810");
//			msg.set(39, "00");
//			// msg.set(70, "301");
//
//			byte[] messageBody = msg.pack();
//			System.out.println("request : " + new String(messageBody));
//			ChannelManager.logISOMsg(msg);
//
//			msg.setPackager(new ISO87APackager());
//			source.send(msg);
//		} catch (ISOException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Send sign-on response.
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendSignOnRequest(ISOSource source, ISOMsg m) {
		System.out.println("\nsendSignOnResponse");

		try {
			ISOMsg msg = (ISOMsg) m.clone();

			msg.setMTI("0800");
			msg.set(7, ISODate.getDateTime(new Date()));
			msg.set(11, "000002");
			msg.set(70, "001");
			msg.setPackager(new ISO87APackager());

			byte[] messageBody = msg.pack();
			System.out.println("request : " + new String(messageBody));
			ChannelManager.logISOMsg(msg);

			source.send(msg);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
	private void sendSignOnRequest2(ISOSource source, ISOMsg m) {
		Map<String, String> date = getDate();
		System.out.println("sendSignOnResponse");
		try {
			m.setMTI("0810");
			m.set(7, date.get("bit7"));
			m.set(11, "000001");
			m.set(70, "001");
			m.setPackager(new ISO87APackager());
			ChannelManager.logISOMsg(m);
			System.out.println("length: " + m.pack().length);
			source.send(m);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ISOMsg createHandshakeISOMsg(String mti, String bit70) throws ISOException {
		Map<String, String> date = getDate();
		ISOMsg m = new ISOMsg();
		m.setMTI(mti);
		m.set(7, date.get("bit7"));
		m.set(11, "000001");
		// m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
		m.set(70, bit70);
		m.setPackager(new ISO87APackager());
		ChannelManager.logISOMsg(m);
		return m;
	}

	private ISOMsg createReversalISOMsg(ISOMsg lateResponse) throws ISOException {
		System.out.println("\ncreateReversalISOMsg");

		ISOMsg m = new ISOMsg();

		m.setMTI("0400");
		m.set(2, lateResponse.getValue(2).toString());
		m.set(3, lateResponse.getValue(3).toString());
		m.set(4, lateResponse.getValue(4).toString());
		m.set(7, lateResponse.getValue(7).toString());
		m.set(11, lateResponse.getValue(11).toString());
		m.set(12, lateResponse.getValue(12).toString());
		m.set(13, lateResponse.getValue(13).toString());
		m.set(15, lateResponse.getValue(15).toString());
		m.set(18, lateResponse.getValue(18).toString());
		m.set(32, lateResponse.getValue(32).toString());
		m.set(37, lateResponse.getValue(37).toString());
		m.set(42, lateResponse.getValue(42).toString());
		m.set(43, lateResponse.getValue(43).toString());
		m.set(48, lateResponse.getValue(48).toString());
		m.set(49, lateResponse.getValue(49).toString());
		m.set(63, lateResponse.getValue(63).toString());
		m.set(90, "0200" + lateResponse.getValue(11).toString() + "" + lateResponse.getValue(7).toString() + "00000"
				+ lateResponse.getValue(32).toString() + "00000000000");
		m.setPackager(new ISO87APackager());
		ChannelManager.logISOMsg(m);

		return m;
	}

	// private ISOMsg createHandshakeISOMsg2(String mti, String bit70) throws
	// ISOException {
	// System.out.println("createHandshakeISOMsg2");
	// ISOMsg m = new ISOMsg();
	// m.setMTI(mti);
	// m.set(7, ISODate.getDateTime(new Date()));
	// m.set(11, "1");
	// // m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
	// m.set(39, "00");
	// m.set(70, bit70);
	// m.setPackager(new ISO87APackager());
	// ChannelManager.logISOMsg(m);
	// return m;
	// }

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
}
