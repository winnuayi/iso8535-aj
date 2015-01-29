package com.ciheul.iso.dummy;

import java.util.Date;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.util.NameRegistrar;

/**
 * Created by galihlasahido on 9/28/14.
 */
public class LogonManager extends QBeanSupport {
    private Space sp = SpaceFactory.getSpace();
    private Space psp = SpaceFactory.getSpace("jdbm:MyTxnSpaceClient");

    private static final String TRACE = "JPTS_TRACE";
    private static final String LOGON = "JPTS_LOGON.";
    private static final String ECHO = "JPTS_ECHO.";
    private long echoInterval = 0;
    private long logonInterval = 0;
    protected QMUX mux = null;
    protected String readyKey = "";
    private Thread echoThread;
    private Thread logonThread;

    @Override
    protected void startService() throws Exception {
        echoInterval = Long.parseLong(cfg.get("echo-interval"));
        logonInterval = Long.parseLong(cfg.get("logon-interval"));
        mux = (QMUX) NameRegistrar.getIfExists("mux." + cfg.get("mux"));
        readyKey = cfg.get("channel-ready");
        getLog().info(readyKey);

        logonThread = new Thread(new Logon());
        logonThread.start();

        echoThread = new Thread(new Echo());
        echoThread.start();

    }

    private void doEcho() throws ISOException {
        readyKey = cfg.get("channel-ready");

        ISOMsg resp = mux.request(doEchoISOMsg(), cfg.getLong("timeout"));
        if (resp != null) {
            NameRegistrar.register(ECHO + readyKey, true);
        } else {
            NameRegistrar.register(LOGON + readyKey, false);
        }
    }

    private void doLogon() {
        readyKey = cfg.get("channel-ready");
        ISOMsg resp = null;
        try {
            resp = mux.request(doLogonISOMsg(), cfg.getLong("timeout"));
            if (resp != null) {
                NameRegistrar.register(LOGON + readyKey, true);
            } else {
                NameRegistrar.register(LOGON + readyKey, false);
            }
        } catch (ISOException e) {
            getLog().warn(e);
        }
    }

    private ISOMsg doEchoISOMsg() {
        long traceNumber = SpaceUtil.nextLong(psp, TRACE) % 100000;
        ISOMsg m = new ISOMsg();
        try {
            m.setMTI("0800");
            m.set(7, ISODate.getDateTime(new Date()));
            m.set(11, ISOUtil.zeropad(Long.toString(traceNumber), 6));
            m.set(70, "301");
        } catch (ISOException e) {
            e.printStackTrace();
        }
        return m;
    }

    private ISOMsg doLogonISOMsg() {
        long traceNumber = SpaceUtil.nextLong(psp, TRACE) % 100000;
        ISOMsg m = new ISOMsg();
        try {
            m.setMTI("0800");
            m.set(7, ISODate.getDateTime(new Date()));
            m.set(11, ISOUtil.zeropad(Long.toString(traceNumber), 6));
            m.set(70, "001");
        } catch (ISOException e) {
            e.printStackTrace();
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    public class Echo implements Runnable {
        public Echo() {
            super();
        }

        public void run() {
            readyKey = cfg.get("channel-ready");
            while (running()) {
                Object sessionId = sp.rd(readyKey, cfg.getLong("timeout"));

                if (sessionId == null) {
                    getLog().info("Channel " + readyKey + " not ready");
                    NameRegistrar.register(LOGON + readyKey, false);
                    continue;
                }

                Boolean registered = (Boolean) NameRegistrar.getIfExists(LOGON + readyKey);
                try {
                    if (registered == null) {
                        doLogon();
                    } else if (registered.equals(true)) {
                        doEcho();
                    } else {
                        doLogon();
                    }
                } catch (Throwable t) {
                    getLog().warn(t);
                }

                ISOUtil.sleep(echoInterval);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public class Logon implements Runnable {
        public Logon() {
            super();
        }

        public void run() {
            readyKey = cfg.get("channel-ready");
            while (running()) {
                getLog().info("Channel " + readyKey + " running ready");
                Object sessionId = sp.rd(readyKey, cfg.getLong("timeout"));

                if (sessionId == null) {
                    getLog().info("Channel " + readyKey + " not ready");
                    NameRegistrar.register(LOGON + readyKey, false);
                    continue;
                }

                doLogon();

                ISOUtil.sleep(logonInterval);
            }
        }
    }
}