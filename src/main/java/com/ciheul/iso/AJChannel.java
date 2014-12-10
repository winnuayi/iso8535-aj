package com.ciheul.iso;

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;

public class AJChannel extends BaseChannel {
	public AJChannel() {
		super();
	}

	public AJChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
	}

	public AJChannel(ISOPackager p) throws IOException {
		super(p);
	}

	public AJChannel(ISOPackager p, ServerSocket serverSocket)
			throws IOException {
		super(p, serverSocket);
	}

	protected void sendMessageLength(int len) throws IOException {
		System.out.println("sendMessageLength: " + len);
		serverOut.write(len >> 8);
		serverOut.write(len);
		serverOut.write(0);
		serverOut.write(0);
	}

	protected int getMessageLength() throws IOException, ISOException {
//		System.out.println("getMessageLength");
		int l = 0;
		int max = 2;
		byte[] b = new byte[max];
		while (l == 0) {
			serverIn.readFully(b, 0, max);
//			for (int i = 0; i < max; i++) {
//				System.out.println(b[i]);
//			}

			l = ((((int) b[0]) & 0xFF) << 8) | (((int) b[1]) & 0xFF);
//			System.out.println("getMessageLength: " + l);
			if (l == 0) {
				serverOut.write(b);
				serverOut.flush();
			}
		}

		return l;
	}

	protected int getHeaderLength() {
//		System.out.println("getHeaderLength");
		// CS Channel does not support header
		return 0;
	}

	protected void sendMessageHeader(ISOMsg m, int len) {
		System.out.println("sendMessageHeader");
		// CS Channel does not support header
	}
}
