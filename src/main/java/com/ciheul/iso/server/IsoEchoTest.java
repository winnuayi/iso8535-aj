package com.ciheul.iso.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;

public class IsoEchoTest {

	private static final Logger logger = Logger
			.getLogger(ChannelManager.class);
	
	private static String stanId = "000001";
	private ChannelManager channelManager;

	public void EchoTest() {

		channelManager = ChannelManager.getInstance();
		Map<String, String> date = getDate();
		ISOMsg reply = null;
		try {

			ISOMsg msg = new ISOMsg();
			msg.setMTI("0800");
			msg.set(7, date.get("bit7"));
			msg.set(11, stanId);
			msg.set(70, "301");
			msg.setPackager(new ISO87APackager());

			byte[] messageBody = msg.pack();
			System.out.println("request : " + new String(messageBody));
			ChannelManager.logISOMsg(msg);
			try {
				reply = channelManager.sendMsg(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("kirim echo test 2");

			if (reply != null) {
				ChannelManager.logISOMsg(reply);
				System.out.println("kirim echo test 4");
			}

		} catch (ISOException e) {
			
			logger.error(e.getMessage());
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
		String bit7 = dateFormat1.format(newDate) + dateFormat2.format(newDate)
				+ dateFormat3.format(newDate);
		String bit12 = dateFormat2.format(newDate)
				+ dateFormat3.format(newDate);
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
