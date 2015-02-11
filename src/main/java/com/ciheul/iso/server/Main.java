package com.ciheul.iso.server;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jpos.iso.ISOException;
import org.jpos.q2.Q2;

import com.ciheul.database.DBConnection;
import com.ciheul.database.DatabaseManager;
import com.ciheul.database.RedisConnection;

public class Main {

    public static void main(String[] args) throws ISOException {
    	//disable All logging
    	LogManager.getRootLogger().setLevel(Level.ERROR);

    	//enable logging except from jetty
    	LogManager.getLogger(IsoServlet.class).setLevel(Level.ALL);
    	LogManager.getLogger(ChannelManager.class).setLevel(Level.ALL);
    	LogManager.getLogger(ClientRequestListener.class).setLevel(Level.ALL);
    	LogManager.getLogger(DatabaseManager.class).setLevel(Level.ALL);
    	LogManager.getLogger(DBConnection.class).setLevel(Level.ALL);
    	LogManager.getLogger(RedisConnection.class).setLevel(Level.ALL);
    	
        Q2 q2 = new Q2(args);
        q2.start();
    }
    
    

}