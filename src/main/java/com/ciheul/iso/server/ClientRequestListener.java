package com.ciheul.iso.server;

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
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

public class ClientRequestListener implements ISORequestListener {
    
    ChannelManager channelManager = ChannelManager.getInstance();

    /**
     * MAIN HANDLER
     */
    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        try {
			channelManager = ((ChannelManager) NameRegistrar.get("manager"));
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        System.out.println("process start");
        ChannelManager.logISOMsg(m);
        try {
            if (m.getMTI().equals("0800")) {
                channelManager.sendMsg(createHandshakeISOMsg2());
//                System.out.println("late response ");
            } else 
        	if (Integer.parseInt(m.getValue(4).toString())>0) {

        		if (m.getMTI().equals("0210")) {
                    if (m.getValue(48).toString().substring(0, 4).equals("2111")) {

                    } else {
                        channelManager.sendMsg(createReversalISOMsg(m));
                    }
                }
			}
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("process end");
        return false;
    }

    private ISOMsg createHandshakeISOMsg() throws ISOException {
        Map<String, String> date = getDate();
        ISOMsg m = new ISOMsg();
        m.setMTI("0800");
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, "1");
        // m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
        m.set(70, "001");
        m.setPackager(new ISO87APackager());
        ChannelManager.logISOMsg(m);
        return m;
    }

    private ISOMsg createReversalISOMsg(ISOMsg lateResponse) throws ISOException {
        System.out.println("createReversalISOMsg");
        Map<String, String> date = getDate();
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
        m.set(90, "0200" + lateResponse.getValue(11).toString() + lateResponse.getValue(7).toString() + lateResponse.getValue(32).toString() + "00000"
                + lateResponse.getValue(32).toString() + "00000000000");
        m.setPackager(new ISO87APackager());
        ChannelManager.logISOMsg(m);

        return m;
    }

    private ISOMsg createHandshakeISOMsg2() throws ISOException {
        System.out.println("createHandshakeISOMsg2");
        ISOMsg m = new ISOMsg();
        m.setMTI("0810");
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, "1");
        // m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
        m.set(39, "00");
        m.set(70, "001");
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
