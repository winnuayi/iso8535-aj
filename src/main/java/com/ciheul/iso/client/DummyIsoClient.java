package com.ciheul.iso.client;

import java.io.IOException;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;

import com.ciheul.iso.AJChannel;

public class DummyIsoClient {

	private ISOChannel channel = new AJChannel("localhost", 2231, new ISO87APackager());	

	private static void logISOMsg(ISOMsg msg) {
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

	private void sendSimpleMessage() throws IOException, ISOException {
		channel.connect();
		
		ISOMsg m = new ISOMsg();
		m.setMTI("0800");
		m.set(3, "333333");
		m.setPackager(new ISO87APackager());
				
		channel.send(m);
		
		ISOMsg reply = channel.receive();
		if (reply == null) {
			return;
		}
		System.out.println("response: " + new String(reply.pack()));
		logISOMsg(reply);
		
		channel.disconnect();
	}

	public static void main(String[] args) throws IOException, ISOException {
		DummyIsoClient client = new DummyIsoClient();
		client.sendSimpleMessage();
	}
}
