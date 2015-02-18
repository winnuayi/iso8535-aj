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
	private ChannelManager channelManager;
	private int state = NOT_LOGGEDIN;

	public ClientRequestListener() {
	}

	/**
	 * MAIN HANDLER
	 */
	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		try {
			logger.info("incoming ISOMSG : " + m.getMTI());
		} catch (ISOException e1) {
			// TODO Auto-generated catch block
			logger.error(e1.getMessage());
		}
		channelManager.logISOMsg(m);
		channelManager = ChannelManager.getInstance();
		System.out.println("process start");
		try {
			if (m.getMTI().equals("0800")) {
				sendEchoTestResponse(source, m);
				sendSignOnRequest(source, m);
			} else if (m.getMTI().equals("0810")) {
				
			} else if (m.getMTI().equals("0410")) {

			} else if (Long.parseLong(m.getValue(4).toString()) > 0) {

				if (m.getMTI().equals("0210")) {
					if (m.getValue(48).toString().substring(0, 4).equals("2111")) {
						String adviceMessage1 = DatabaseManager.getAdvice(m.getValue(48).toString().substring(15, 27));
						String adviceMessage2 = DatabaseManager.getAdvice(m.getValue(48).toString().substring(4, 15));
						String rc = m.getValue(39).toString();
						if (m.getValue(39).toString().equals("68")) {
							rc = rc + "2";
						}
						if ((adviceMessage1 == null || adviceMessage2 == null)) {
							String msgBytes = m.getValue(4).toString() + "#" + rc + "#" + m.getValue(48).toString();
							DatabaseManager.setAdviceSuccess("" + Integer.parseInt(m.getValue(37).toString()), msgBytes);
						} else {
							DatabaseManager.updateBit48(m.getValue(48).toString().substring(4, 15), m.getValue(48)
									.toString().substring(15, 27), "" + Integer.parseInt(m.getValue(37).toString()), m
									.getValue(48).toString(), Context.PENDING_STATUS);
							if (m.getValue(39).toString().equals("00")) {
								DatabaseManager.updateStatusTransaction("" + m.getValue(37), Context.SUCCESS_STATUS, m
										.getValue(39).toString(), "Approved");
							} else {
								DatabaseManager.updateStatusTransaction("" + m.getValue(37), Context.FAIL_STATUS, rc,
										"Transaction Fail");
							}
							DatabaseManager.delAdvice(m.getValue(48).toString().substring(15, 27));
							DatabaseManager.delAdvice(m.getValue(48).toString().substring(4, 15));
						}
					} else {
						channelManager.sendMsg(createReversalISOMsg(m));
					}
				}
			}
		} catch (ISOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
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
	private void sendEchoTestResponse(ISOSource source, ISOMsg m) {
		try {
			ISOMsg msg = (ISOMsg) m.clone();
			msg.setMTI("0810");
			msg.set(39, "00");
			byte[] messageBody = msg.pack();
			
			ChannelManager.logISOMsg(msg);
			msg.setPackager(new ISO87APackager());
			source.send(msg);
		} catch (ISOException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
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
	private void sendSignOnRequest(ISOSource source, ISOMsg m) {
		try {
			ISOMsg msg = (ISOMsg) m.clone();
			msg.setMTI("0800");
			msg.set(7, ISODate.getDateTime(new Date()));
			msg.set(11, "000002");
			msg.set(70, "001");
			msg.setPackager(new ISO87APackager());
			byte[] messageBody = msg.pack();

			ChannelManager.logISOMsg(msg);
			source.send(msg);
		} catch (ISOException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Send late payment response.
	 * Send auto reversal
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private ISOMsg createReversalISOMsg(ISOMsg lateResponse) throws ISOException {
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

	/**
	 * Date format for bit7, bit12, bit13, bit15
	 * 
	 * return Hashmap
	 */
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
