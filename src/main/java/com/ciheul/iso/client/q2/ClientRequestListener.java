package com.ciheul.iso.client.q2;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

public class ClientRequestListener implements ISORequestListener {

	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		ChannelManager.logISOMsg(m);
		System.out.println("wowwww! process ISORequestListener");
		return false;
	}

}
