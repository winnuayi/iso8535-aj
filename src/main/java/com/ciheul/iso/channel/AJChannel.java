package com.ciheul.iso.channel;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

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
		serverOut.write(len >> 8);
		serverOut.write(len);
		serverOut.write(0);
		serverOut.write(0);
	}

	protected int getMessageLength() throws IOException, ISOException {
		int l = 0;
		int max = 2;
		byte[] b = new byte[max];
		while (l == 0) {
			serverIn.readFully(b, 0, max);
			l = ((((int) b[0]) & 0xFF) << 8) | (((int) b[1]) & 0xFF);
			if (l == 0) {
				serverOut.write(b);
				serverOut.flush();
			}
		}

		return l;
	}

	protected int getHeaderLength() {
		// AJ Channel does not support header
		return 0;
	}

	protected void sendMessageHeader(ISOMsg m, int len) throws IOException {
		// AJ Channel does not support header
	}	
	
	/**
     * sends a byte[] over the TCP/IP session
     * @param b the byte array to be sent
     * @exception IOException
     * @exception ISOException
     * @exception ISOFilter.VetoException;
     */
    public void send (ISOMsg m) 
        throws IOException, ISOException
    {
        LogEvent evt = new LogEvent (this, "send");
        try {
            if (!isConnected())
                throw new ISOException ("unconnected ISOChannel");
            byte[] b = createMessageAJ(m);
            synchronized (serverOutLock) {
                serverOut.write(b);
                serverOut.flush();
            }
            cnt[TX]++;
            setChanged();
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ISOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
        }
    }    
    
    private byte[] concat(byte[] array1, byte[] array2) {
		byte[] result = new byte[array1.length + array2.length];

		for (int i = 0; i < array1.length; i++) {
			result[i] = array1[i];
		}

		for (int j = 0; j < array2.length; j++) {
			result[array1.length + j] = array2[j];
		}
		return result;
	}
    
    private byte[] createMessageAJ(ISOMsg msg) throws ISOException {
		byte[] messageBody = msg.pack();

		short messageLength = (short) messageBody.length;

		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short) messageLength);
		byte[] header = bb.array();
		return concat(header, messageBody);
	}
}
