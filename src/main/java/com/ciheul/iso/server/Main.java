package com.ciheul.iso.server;

import org.jpos.iso.ISOException;
import org.jpos.q2.Q2;

public class Main {

    public static void main(String[] args) throws ISOException {
        Q2 q2 = new Q2(args);
        q2.start();
//        try {
//			q2.main(args);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

}