package com.ciheul.iso.server;

import java.util.List;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

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
            sendLinkUp(m);
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
            	sendLinkUp(m);
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

    public void sendLinkUp(ISOMsg m){

    	String billNumber = "";
		try {
			if (m.getValue(48).toString().substring(0, 4).equals("2112")) {

				billNumber = m.getValue(48).toString().substring(4, 16);
			}else if (m.getValue(48).toString().substring(0, 4).equals("2114")) {

				billNumber = m.getValue(48).toString().substring(4, 17);
			}
	    	String reversalMessage = m.getValue(4).toString()
	    			+"#"
	    			+m.getValue(7).toString()
	    			+"#"
	    			+m.getValue(11).toString()
	    			+"#"
	    			+m.getValue(37).toString()
	    			+"#"
	    			+m.getValue(42).toString()
	    			+"#"
	    			+m.getValue(48).toString()
	    			+"#"
	    			+"0200" + m.getValue(11).toString() + m.getValue(7).toString() + m.getValue(32).toString() + "00000000000"
	    			+"#"
	    			+billNumber;
	    	System.out.println("masuk :"+reversalMessage);
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