package com.ciheul.iso.client.q2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.packager.ISO87APackager;

public class ClientRequestListener implements ISORequestListener {

	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		ChannelManager.logISOMsg(m);
		System.out.println("wowwww! process ISORequestListener");
		return false;
	}
	

    private ISOMsg createHandshakeISOMsg() throws ISOException {
        ISOMsg m = new ISOMsg();
        m.setMTI("0800");
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
        m.set(70, "301");
        m.setPackager(new ISO87APackager());
        ChannelManager.logISOMsg(m);
        return m;
    }

    private ISOMsg createHandshakeISOMsg2() throws ISOException {
        ISOMsg m = new ISOMsg();
        m.setMTI("0810");
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
        m.set(39, "00");
        m.set(70, "301");
        m.setPackager(new ISO87APackager());
        ChannelManager.logISOMsg(m);
        return m;
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
}
