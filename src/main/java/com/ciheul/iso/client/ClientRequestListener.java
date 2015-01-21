package com.ciheul.iso.client;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

public class ClientRequestListener implements ISORequestListener {

	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		System.out.println("wowwww! process ISORequestListener");
		return false;
	}

}
