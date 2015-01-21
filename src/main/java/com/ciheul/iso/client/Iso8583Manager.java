//package com.ciheul.iso.client;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Date;
//
//import org.jpos.core.Sequencer;
//import org.jpos.core.VolatileSequencer;
//import org.jpos.iso.ISOChannel;
//import org.jpos.iso.ISODate;
//import org.jpos.iso.ISOException;
//import org.jpos.iso.ISOField;
//import org.jpos.iso.ISOMsg;
//import org.jpos.iso.ISORequestListener;
//import org.jpos.iso.ISOSource;
//import org.jpos.iso.ISOUtil;
//import org.jpos.iso.channel.ASCIIChannel;
//import org.jpos.iso.packager.GenericPackager;
//import org.jpos.util.Log;
//import org.jpos.util.LogSource;
//import org.jpos.util.Logger;
//import org.jpos.util.SimpleLogListener;
//
//public class Iso8583Manager {
//	protected static final Log log = LogFactory.getLog(Iso8583Manager.class);
//	protected ISOChannel channel;
//	protected ISOMUX mux;
//	protected GenericPackager packager;
//	protected Sequencer seq = new VolatileSequencer();
//	protected Sequencer fileSeq = new FileSequencer();
//
//	protected String host;
//	protected int port;
//	protected int timeout;
//	protected boolean echoSent;
//	protected int echoInterval;
//
//	public Iso8583Manager(String host, int port, int timeout) {
//		this.host = host;
//		this.port = port;
//		this.timeout = timeout;
//		start();
//	}
//
//	public void setChannel(ISOChannel channel) {
//		this.channel = channel;
//	}
//
//	public void setTimeout(int timeout) {
//		this.timeout = timeout;
//	}
//
//	public String getHost() {
//		return host;
//	}
//
//	public int getPort() {
//		return port;
//	}
//
//	public void start() {
//		InputStream isp = Iso8583Manager.class.getClassLoader().getResourceAsStream("metadata/iso87ascii.xml");
//		try {
//			packager = new GenericPackager(isp);
//			channel = new ASCIIChannel(host, port, packager);
//			// channel.connect();
//
//			// if(mux!=null){
//			mux = new ISOMUX(channel);
//			// }
//
//			new Thread(mux).start();
//
//			mux.setISORequestListener(new ISORequestListener() {
//
//				@Override
//				public boolean process(ISOSource source, ISOMsg isoMsg) {
//					try {
//						System.out.println("get iso: " + new String(isoMsg.pack()));
//					} catch (ISOException e) {
//						log.error("Exception Message", e);
//					}
//					processIso(isoMsg);
//					return false;
//				}
//			});
//
//			Logger logger = new Logger();
//			logger.addListener(new SimpleLogListener(System.out));
//			((LogSource) channel).setLogger(logger, "channel");
//			mux.setLogger(logger, "mux");
//
//		} catch (ISOException e) {
//			log.error("Exception Message", e);
//		}
//
//		new Thread(new EchoThread()).start();
//
//	}
//
//	public ISOMsg send(ISOMsg isoMsg) throws IOException {
//		checkConnection();
//
//		// try {
//		// log.debug("send to vlink: "+isoMsg.pack());
//		// } catch (ISOException e) {
//		// log.error("Exception Message",e);
//		// } catch (Exception e){
//		// log.error("Exception Message",e);
//		// }
//		ISORequest req = new ISORequest(isoMsg);
//		mux.queue(req);
//		ISOMsg isoResponse = req.getResponse(timeout * 1000);
//		if (isoResponse == null) {
//			// reset connection
//			channel = null;
//			channel = new ASCIIChannel(host, port, packager);
//		}
//		return isoResponse;
//	}
//
//	public void sendReply(ISOMsg isoMsg) throws IOException {
//		checkConnection();
//
//		try {
//			isoMsg.setMTI("0210");
//			log.debug("send reply vlink: " + new String(isoMsg.pack()));
//		} catch (ISOException e) {
//			log.error("Exception Message", e);
//		}
//		mux.send(isoMsg);
//	}
//
//	protected void processIso(ISOMsg isoMsg) {
//
//		try {
//			// echo
//			if (isoMsg.getMTI().equals("0800")) {
//				log.debug("replying iso-echo");
//				isoMsg.setMTI("0810");
//				isoMsg.set(new ISOField(39, "00"));
//				send(isoMsg);
//
//				return;
//			}
//
//			// processing notification
//
//		} catch (ISOException e1) {
//			e1.printStackTrace();
//		} catch (IOException e) {
//			log.error("Exception Message", e);
//		}
//	}
//
//	public boolean isOnline() {
//		if (channel != null) {
//			return channel.isConnected();
//		}
//		return false;
//	}
//
//	public boolean isEchoSent() {
//		return echoSent;
//	}
//
//	public void setEchoSent(boolean echoSent) {
//		this.echoSent = echoSent;
//	}
//
//	public int getEchoInterval() {
//		return echoInterval;
//	}
//
//	public void setEchoInterval(int echoInterval) {
//		this.echoInterval = echoInterval;
//	}
//
//	private void checkConnection() {
//		if (!channel.isConnected()) {
//			try {
//				channel.connect();
//			} catch (IOException e) {
//				log.info("connection down, reconnect channel");
//				try {
//					channel.disconnect();
//				} catch (IOException e1) {
//					log.error("Exception Message", e);
//				}
//				channel = null;
//				channel = new ASCIIChannel(host, port, packager);
//
//			}
//		}
//	}
//
//	private class EchoThread implements Runnable {
//
//		@Override
//		public void run() {
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			while (true) {
//				checkConnection();
//				if (echoSent) {
//					try {
//						ISOMsg isoMsg = new ISOMsg();
//						try {
//							isoMsg.setMTI("0800");
//							Date d = new Date();
//							String traceNumber = ISOUtil.zeropad(new Integer(seq.get("traceno")).toString(), 6);
//							isoMsg.set(new ISOField(7, ISODate.getDateTime(d)));
//							isoMsg.set(new ISOField(18, "6014")); // VLINK
//																	// Purposes
//							isoMsg.set(new ISOField(11, traceNumber));
//							isoMsg.set(new ISOField(70, "301"));
//						} catch (ISOException e) {
//							log.error("Exception Message", e);
//						}
//
//						System.out.println("send: " + isoMsg);
//						ISOMsg isoReply = send(isoMsg);
//						if (isoReply == null) {
//							System.err.println("connection error");
//						}
//					} catch (IOException e) {
//						log.error("Exception Message", e);
//					}
//				}
//				try {
//					Thread.sleep(echoInterval * 1000);
//				} catch (InterruptedException e) {
//					log.error("Exception Message", e);
//				}
//			}
//		}
//
//	}
//
//}
