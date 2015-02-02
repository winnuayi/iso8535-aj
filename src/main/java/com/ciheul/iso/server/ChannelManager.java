package com.ciheul.iso.server;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

import com.ciheul.iso.AJMUX;

public class ChannelManager extends QBeanSupport implements SpaceListener {
    private static ChannelManager _cMSingleTon = null;
    private long MAX_TIME_OUT;
    private AJMUX mux;
    private LocalSpace sp;
    private String in;
    private String out;

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

    public ISOMsg sendMsg(ISOMsg m) throws Exception {
        return sendMsg(m, mux, MAX_TIME_OUT);
    }

    private ISOMsg sendMsg(ISOMsg msg, AJMUX mux, long timeout) throws Exception {
        if (mux != null) {
            System.out.println("mux sending message...");
            long start = System.currentTimeMillis();

            ISOMsg resp = null;
            
            // append message to 'out' queue
            sp.out(out, msg, timeout);

            // wait till exists and take away message from 'in' queue
            Object obj = sp.in(in, timeout);

            // success to receive response message from server
            if (obj instanceof ISOMsg) {
                resp = (ISOMsg) obj;
                logISOMsg(resp);
            }

            // link down
            if (obj instanceof String) {
                System.out.println((String) obj);
            }
            
            // timeout
            if (obj == null) {
                System.out.println("TIMEOUT BRO");
            }

            long duration = System.currentTimeMillis() - start;
            log.info("Response time (ms):" + duration);
            return resp;
        }

        System.out.println("mux is null");
        return null;
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
        System.out.println(key);
        System.out.println(value);
        // Object obj = sp.inp(key);
        // if (obj instanceof ISOMsg) {
        // ISOMsg msg = (ISOMsg) obj;
        // logISOMsg(msg);
        // }
    }
}