package com.ciheul.iso.client.useless;

import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.MUX;
import org.jpos.q2.Q2;
import org.jpos.util.NameRegistrar;

public class ServerListener implements ISORequestListener {

	public static void main(String[] args) {
		Q2 q2 = new Q2();
		q2.start();
	}

	@Override
	public boolean process(ISOSource isoSrc, ISOMsg isoMsg) {

		try {
			if (isoMsg.getMTI().equals("0800")) {
				ISOMsg reply = (ISOMsg) isoMsg.clone();
				reply.setResponseMTI();
				reply.set(39, "00");
				isoSrc.send(reply);
				return true;
			}
			// send request to server B
			MUX mux = (MUX) NameRegistrar.getIfExists("mux.mymux");
//			MUX mux = (MUX) NameRegistrar.getIfExists("mux.jpos-client-mux");
			ISOMsg reply = mux.request(isoMsg, 10 * 1000);
			if (reply != null) {
				System.out.println(new String(reply.pack()));
				reply.set(125, "RESPONSE FROM SERVER A");
				isoSrc.send(reply);
			}
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
