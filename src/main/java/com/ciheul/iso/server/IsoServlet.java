// In [85]: headers = { 'Content-type': 'application/json', 'Accept': 'application/json' }
// In [86]: payload = { 'message': 'hello world!' }
// In [87]: requests.post('http://localhost:9092/api/send', data=json.dumps(payload), headers=headers).text
// Out[87]: u'{"message":"ciheul"}'

package com.ciheul.iso.server;

import java.text.SimpleDateFormat;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

@Path("/api")
public class IsoServlet {
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(IsoServlet.class);
	ChannelManager channelManager = null;
	ChannelManager channelManagerPrepaid = null;
	ChannelManager channelManagerPostpaidNTL = null;

	public IsoServlet() {
		// channelManager = ChannelManager.getInstance();
		try {
			channelManagerPrepaid = ((ChannelManager) NameRegistrar.get("manager"));
			channelManagerPostpaidNTL = ((ChannelManager) NameRegistrar.get("aj-postpaid-ntl-manager"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	@POST
	@Path("/send")
	@Produces(MediaType.APPLICATION_JSON)
	public IsoMessageResponse send(IsoMessageRequest msg) {

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		java.util.Date now2 = new java.util.Date();
		String strDate2 = sdf2.format(now2);

//		logger.info("incoming from PGW :" + msg);

		String responseMsg = "";
		ISOMsg resp = null;
		String[] isoMsgSplit = msg.getMessage().split("#");
		String isoMsgSend = msg.getMessage();

		// now, AJ has two servers. one for prepaid (2111) and another one for postpaid (2112) + NTL (2114)
		if (isoMsgSplit[14].substring(0, 4).equals("2111")) {
			channelManager = channelManagerPrepaid;
		} else {
			channelManager = channelManagerPostpaidNTL;
		}

		switch (isoMsgSplit[0]) {

		// Send MTI 0800
		case "0800":
			try {
				resp = channelManager.sendMsg(createHandshakeISOMsg(isoMsgSend));

//				logger.info("response : ");
				ChannelManager.logISOMsg(resp);

				if (resp != null) {
					responseMsg = resp.getValue(39).toString();
				}
			} catch (ISOException e) {
				responseMsg = e.getMessage();

				logger.error(responseMsg);
			} catch (Exception e) {
				responseMsg = e.getMessage();

				logger.error(responseMsg);
			}
			break;

		// Send MTI 0810
		case "0810":
			try {
				resp = channelManager.sendMsg(createHandshakeISOMsg2(isoMsgSend));

//				logger.info("response : ");
				ChannelManager.logISOMsg(resp);

				if (resp != null) {
					responseMsg = resp.getValue(39).toString();
				}
			} catch (ISOException e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			} catch (Exception e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			}
			break;

		// Send MTI 0200
		case "0200":
			try {
				System.out.println("message request : " + isoMsgSend);

				boolean isAdvice = false;
				if (isoMsgSplit.length > 13) {
					if (isoMsgSplit[14].substring(isoMsgSplit[14].length() - 1).equals("A")) {
						isAdvice = true;
					}
				}

				resp = channelManager.sendMsg(createSendInquiryISOMsg(isoMsgSend));
//				logger.info("response : ");

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				java.util.Date now = new java.util.Date();
				String strDate = sdf.format(now);

//				logger.error("cek time q2 receive : " + strDate);

				ChannelManager.logISOMsg(resp);
				// <<<<<<< HEAD

				if (resp != null 
						&& !(resp.getValue(39).toString().equals("404") && resp.getMTI().equals("0200"))
						&& !(resp.getValue(39).toString().equals("68") && resp.getMTI().equals("0200"))
						&& !(resp.getValue(39).toString().equals("13") && resp.getValue(3).equals("180000"))
						&& !(resp.getValue(39).toString().equals("63") && resp.getValue(3).equals("180000"))) {
					responseMsg = resp.getValue(4).toString() + "#" + resp.getValue(39).toString() + "#"
							+ resp.getValue(48);					
				} else {
					if (resp != null) {
						System.out.println("message from AJ : " + resp.getValue(39).toString());						
						// >>>>>>> support-#5
						switch (resp.getValue(39).toString()) {
						case "404":
							responseMsg = "TIMEOUT";
							logger.info("LINK DOWN");
							break;
						case "68":
							responseMsg = "TIMEOUT";
							logger.info("REQUEST TIMEOUT");
							break;
						case "13":
							responseMsg = "NOMINAL PEMBELIAN TIDAK TERDAFTAR";
							logger.info("NOMINAL PEMBELIAN TIDAK TERDAFTAR");

							// responseMsg = "TIMEOUT13";
							// logger.info("REQUEST TIMEOUT");
							break;
						case "63":
							responseMsg = "TIMEOUT63";
							logger.info("REQUEST TIMEOUT");
							break;
						default:
							break;
						}
					}
				}
			} catch (ISOException e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			} catch (Exception e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			}
			break;

		// Send MTI 0210
		case "0210":
			break;

		// Send MTI 0400
		case "0400":
			try {
			    channelManager = channelManagerPostpaidNTL;
				resp = channelManager.sendMsg(createSendReversalISOMsg(isoMsgSend));

				logger.info("response : ");

				ChannelManager.logISOMsg(resp);
				if (resp != null && !resp.getValue(39).toString().equals("68")) {
					responseMsg = resp.getValue(4).toString() + "#" + resp.getValue(39).toString() + "#"
							+ resp.getValue(48);
					logger.info("response : " + responseMsg);
				} else {
					responseMsg = "TIMEOUT";
					logger.info("REQUEST TIMEOUT");
				}
			} catch (ISOException e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			} catch (Exception e) {
				responseMsg = e.getMessage();
				logger.error(responseMsg);
			}
			break;

		// Send MTI 0410
		case "0410":
			break;

		default:
			break;
		}

		IsoMessageResponse response = new IsoMessageResponse();
		response.setMessage(responseMsg);

//		logger.info("response for PGW :" + response.getMessage());

		return response;
	}

	/**
	 * Setting Iso Message for sending to Artajasa Request from PGW when sending 0800 message
	 */
	private ISOMsg createHandshakeISOMsg(String isoMsgSend) throws ISOException {
		String[] isoMsgSplit = isoMsgSend.split("#");
		ISOMsg m = new ISOMsg();
		m.setMTI(isoMsgSplit[0]);
		m.set(7, isoMsgSplit[1]);
		m.set(11, isoMsgSplit[2]);
		m.set(70, isoMsgSplit[3]);
		m.setPackager(new ISO87APackager());

		ChannelManager.logISOMsg(m);

		return m;
	}

	/**
	 * Setting Iso Message for sending to Artajasa Request from PGW when sending 0810 message
	 */
	private ISOMsg createHandshakeISOMsg2(String isoMsgSend) throws ISOException {
		String[] isoMsgSplit = isoMsgSend.split("#");
		ISOMsg m = new ISOMsg();
		m.setMTI(isoMsgSplit[0]);
		m.set(7, isoMsgSplit[1]);
		m.set(11, isoMsgSplit[2]);
		m.set(39, isoMsgSplit[3]);
		m.set(70, isoMsgSplit[4]);
		m.setPackager(new ISO87APackager());

		ChannelManager.logISOMsg(m);

		return m;
	}

	/**
	 * Setting Iso Message for sending to Artajasa Request from PGW when sending 0400 message
	 */
	private ISOMsg createSendReversalISOMsg(String isoMsgSend) throws ISOException {
		String[] isoMsgSplit = isoMsgSend.split("#");
		ISOMsg m = new ISOMsg();
		m.setMTI(isoMsgSplit[0]);
		m.set(2, isoMsgSplit[1]);
		m.set(3, isoMsgSplit[2]);
		m.set(4, isoMsgSplit[3]);
		m.set(7, isoMsgSplit[4]);
		m.set(11, isoMsgSplit[5]);
		m.set(12, isoMsgSplit[6]);
		m.set(13, isoMsgSplit[7]);
		m.set(15, isoMsgSplit[8]);
		m.set(18, isoMsgSplit[9]);
		m.set(32, isoMsgSplit[10]);
		m.set(37, isoMsgSplit[11]);
		m.set(42, isoMsgSplit[12]);
		m.set(43, isoMsgSplit[13]);
		m.set(48, isoMsgSplit[14]);
		m.set(49, isoMsgSplit[15]);
		m.set(63, isoMsgSplit[16]);
		m.set(90, "0200" + isoMsgSplit[5] + isoMsgSplit[4] + isoMsgSplit[10] + "00000");
		m.setPackager(new ISO87APackager());

		ChannelManager.logISOMsg(m);

		return m;
	}

	/**
	 * Setting Iso Message for sending to Artajasa Request from PGW when sending 0200 message Request for inquiry and
	 * payment
	 */
	private ISOMsg createSendInquiryISOMsg(String isoMsgSend) throws ISOException {
		String[] isoMsgSplit = isoMsgSend.split("#");
//		logger.info("bit48 : " + isoMsgSplit[14]);
		ISOMsg m = new ISOMsg();
		m.setMTI(isoMsgSplit[0]);
		m.set(2, isoMsgSplit[1]);
		m.set(3, isoMsgSplit[2]);
		m.set(4, isoMsgSplit[3]);
		m.set(7, isoMsgSplit[4]);
		m.set(11, isoMsgSplit[5]);
		m.set(12, isoMsgSplit[6]);
		m.set(13, isoMsgSplit[7]);
		m.set(15, isoMsgSplit[8]);
		m.set(18, isoMsgSplit[9]);
		m.set(32, isoMsgSplit[10]);
		m.set(37, isoMsgSplit[11]);
		m.set(42, isoMsgSplit[12]);
		m.set(43, isoMsgSplit[13]);
		// m.set(48, isoMsgSplit[14].replace("\\\\", "#:|:#").replace("\\", "").replace("#:|:#", "\\"));
		m.set(48, isoMsgSplit[14]);
		m.set(49, isoMsgSplit[15]);
		m.set(63, isoMsgSplit[16]);
		m.setPackager(new ISO87APackager());

		ChannelManager.logISOMsg(m);

		return m;
	}

	private void channelConnection() {
	}
}
