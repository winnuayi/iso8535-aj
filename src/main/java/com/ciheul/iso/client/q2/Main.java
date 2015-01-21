package com.ciheul.iso.client.q2;

import java.util.Date;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
 
/**
 * Created with IntelliJ IDEA.
 * User: ouwaifo
 * Date: 6/7/12
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
 
    static void start() {
        Q2 q2 = new Q2();
        q2.start();
    }

    public static void main(String[] args) throws ISOException {
        start();
        ISOUtil.sleep(1000);
        new Thread(new Exec()).start();
    }
 
    static class Exec implements Runnable {
 
        ChannelManager channelManager;
 
        Exec() throws ISOException {
            try {
                channelManager = ((ChannelManager) NameRegistrar.get("manager"));
            } catch (NameRegistrar.NotFoundException e) {
                LogEvent evt = channelManager.getLog().createError();
                evt.addMessage(e);
                evt.addMessage(NameRegistrar.getInstance());
                Logger.log(evt);
            } catch (Throwable t) {
                channelManager.getLog().error(t);
            }
 
        }
 
        private ISOMsg createHandshakeISOMsg() throws ISOException {
            ISOMsg m = new ISOMsg();
            m.setMTI("0800");
            m.set(7, ISODate.getDateTime(new Date()));
            m.set(11, String.valueOf(System.currentTimeMillis() % 1000000));
            m.set(32, "00001603307");
            m.set(41, "T1603307");
            m.set(70, "301");
            return m;
        }
 
        private void sendHandShake() throws Exception {
            try {
                channelManager.sendMsg(createHandshakeISOMsg());
                channelManager.getLog().info("Handshake sent! ");
            } catch (ISOException e1) {
                channelManager.getLog().error("ISOException :" + e1.getMessage());
            } catch (Exception e) {
                channelManager.getLog().error("Exception :" + e.getMessage());
            }
        }
 
        @Override
        public void run() {
        	try {
                sendHandShake();
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        	System.out.println("finish.");
        	
//            while (true) {
//                ISOUtil.sleep(10000);
//                try {
//                    sendHandShake();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}