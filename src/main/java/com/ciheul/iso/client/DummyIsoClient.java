// http://extendit.us/more-on-jpos-jposee-client-and-server-simulators/
// http://didikhari.web.id/java/jpos-server-forward-request-message-to-another-server/
// http://indonesiakuterkini.blogspot.com/2011/02/re-bls-jug-indonesia-helpisomux_2703.html

package com.ciheul.iso.client;

import java.io.IOException;
import java.net.SocketException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.q2.QBeanSupport;

import com.ciheul.iso.AJChannel;

public class DummyIsoClient extends QBeanSupport {

	private AJChannel channel = new AJChannel("localhost", 2231, new ISO87APackager());

	private static void logISOMsg(ISOMsg msg) {
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

	private void sendSimpleMessage() {
		try {
			channel.connect();

			ISOMsg m = new ISOMsg();
			m.setMTI("0800");
			m.set(3, "333333");
			m.setPackager(new ISO87APackager());

			channel.send(m);

			ISOMsg reply = channel.receive();
			if (reply != null) {
				System.out.println("response: " + new String(reply.pack()));
				logISOMsg(reply);
			} else {
				System.out.println("send reversal");
			}
		} catch (SocketException e) {
			System.out.println("SocketException");
		} catch (IOException e) {
			System.out.println("IOException");
		} catch (ISOException e) {
			System.out.println("ISOException");
		} finally {
			try {
				channel.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		DummyIsoClient client = new DummyIsoClient();
		client.sendSimpleMessage();
	}

}
