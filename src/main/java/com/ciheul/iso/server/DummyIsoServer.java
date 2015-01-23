package com.ciheul.iso.server;

import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import com.ciheul.iso.channel.AJChannel;

public class DummyIsoServer implements ISORequestListener {
	public static String ECHO_REQ = "0800";
	public static String ECHO_RES = "0810";
	public static String TRX_REQ = "0200";

	public DummyIsoServer() {
		super();
	}

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

	private void sendEchoTestResponse(ISOSource source, ISOMsg m) {
		System.out.println("sendEchoResponse");
		 try {
		 Thread.sleep(10000); //1000 milliseconds is one second.
		 } catch(InterruptedException ex) {
		 Thread.currentThread().interrupt();
		 }
		try {
			m.setResponseMTI();
			m.set(39, "88");
			m.set(70, "001");
			m.setPackager(new ISO87APackager());
			logISOMsg(m);
			source.send(m);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns sign-on response
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	private void sendSignOnResponse(ISOSource source, ISOMsg m) {
		System.out.println("sendSignOnResponse");
		try {
			m.set(39, "00");
			m.set(70, "001");
			m.setPackager(new ISO87APackager());
			source.send(m);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Route incoming message to appropriate handler
	 * 
	 * @param source
	 *            (Artajasa) channel
	 * @param m
	 *            message from client
	 */
	public boolean process(ISOSource source, ISOMsg m) {
		System.out.println("ROUTING MESSAGE");
		try {
			if (m.getMTI().equals(ECHO_REQ)) {
				sendEchoTestResponse(source, m);
			} else if (m.getMTI().equals(ECHO_RES)) {
				sendSignOnResponse(source, m);
			} else if (m.getMTI().equals(TRX_REQ)) {

			} else {
				return false;
			}
		} catch (ISOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("main start");
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener(System.out));

		ServerChannel channel = new AJChannel(new ISO87APackager());
		// ServerChannel channel = new XMLChannel(new XMLPackager());
		((LogSource) channel).setLogger(logger, "channel");

		ISOServer server = new ISOServer(2231, channel, null);
		server.setLogger(logger, "server");
		server.addISORequestListener(new DummyIsoServer());

		new Thread(server).start();

		System.out.println("main end");
	}
}
