package com.ciheul.iso.client.q2;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.util.NameRegistrar;

public class ChannelManager extends QBeanSupport {

	private static ChannelManager _cMSingleTon = null;
	private long MAX_TIME_OUT;
	private QMUX mux;

//	public class AsyncListener implements ISOResponseListener {
//
//		@Override
//		public void responseReceived(ISOMsg resp, Object handBack) {
//			System.out.println("responseReceived");			
//		}
//
//		@Override
//		public void expired(Object handBack) {
//			System.out.println("expired");			
//		}
//
//	}

	
	public static void logISOMsg(ISOMsg msg) {
		System.out.println("----ISO MESSAGE-----");
		try {
			System.out.println("  MTI : " + msg.getMTI());
			for (int i = 1; i <= msg.getMaxField(); i++) {
				if (msg.hasField(i)) {
					System.out.println("    Field-" + i + " : "
							+ msg.getString(i));
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
			mux = (QMUX) NameRegistrar.get("mux." + cfg.get("mux"));
			MAX_TIME_OUT = cfg.getLong("timeout");
			NameRegistrar.register("manager", this);
			mux.addISORequestListener(new ClientRequestListener());

		} catch (NameRegistrar.NotFoundException e) {
			log.error("Error in initializing service :" + e.getMessage());
		}
	}

	public ISOMsg sendMsg(ISOMsg m) throws Exception {
		return sendMsg(m, mux, MAX_TIME_OUT);
	}

	private ISOMsg sendMsg(ISOMsg msg, MUX mux, long time) throws Exception {
		if (mux != null) {
			long start = System.currentTimeMillis();
			ISOMsg respMsg = mux.request(msg, 5000);
//			ISOMsg respMsg = null;
//			mux.request(msg, 8000, new AsyncListener(), respMsg);
			long duration = System.currentTimeMillis() - start;
			log.info("Response time (ms):" + duration);
			return respMsg;
		}
		return null;
	}

	public static ChannelManager getInstance(){
		if (_cMSingleTon == null) {
			
			_cMSingleTon = new ChannelManager();
		}
		return _cMSingleTon;
	}
}