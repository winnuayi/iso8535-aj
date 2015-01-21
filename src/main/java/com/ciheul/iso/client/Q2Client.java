package com.ciheul.iso.client;

import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.MUX;
import org.jpos.q2.Q2;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

public class Q2Client implements ISORequestListener {
	MUX mux = null;

	@Override
	public boolean process(ISOSource isoSrc, ISOMsg isoMsg) {
		System.out.println("reversal!");

		try {
			if (isoMsg.getMTI().equals("0800")) {
				ISOMsg reply = (ISOMsg) isoMsg.clone();
				reply.setResponseMTI();
				reply.set(39, "00");
				isoSrc.send(reply);
				return true;
			}
			// send request to server B
			MUX mux = (MUX) NameRegistrar.get("mux.clientsimulator-mux");
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
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	static class Exec implements Runnable {
		MUX mux = null;

		Exec() throws ISOException {
			System.out.println("exec init");

			try {
				mux = (MUX) NameRegistrar.get("mux.clientsimulator-mux");
			} catch (NameRegistrar.NotFoundException e) {
				e.printStackTrace();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		@Override
		public void run() {
			System.out.println("run");
		}
	}

	public static void main(String[] args) throws ISOException {
		NameRegistrar.getInstance().dump(System.out, "debug> ");
		Q2 q2 = new Q2();
		q2.start();

		new Thread(new Exec()).start();
	}
}
