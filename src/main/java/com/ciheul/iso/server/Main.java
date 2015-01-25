package com.ciheul.iso.server;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

public class Main {

    static class Exec implements Runnable {

        ChannelManager channelManager = ChannelManager.getInstance();

        Exec() throws ISOException {
            // TODO consider to move into "ChannelManager.getInstance()" method
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

        @Override
        public void run() {
        }
    }

    public static void main(String[] args) throws ISOException {
        Q2 q2 = new Q2();
        q2.start();

        ISOUtil.sleep(5000);
        new Thread(new Exec()).start();
    }
}