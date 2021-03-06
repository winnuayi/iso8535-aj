package com.ciheul.iso.dummy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;

import com.ciheul.iso.channel.AJChannel;

public class IsoClient {
	// private static String host = "localhost";
	private static String host = "118.97.191.109";
	private static int port = 2231;
	private static ISOChannel channel = new AJChannel(host, port, new ISO87APackager());
	private static String bit48 = "";
	private static String bit39 = "";

	private static HashMap<String, String> getDate() {
		DateFormat dateFormat1 = new SimpleDateFormat("MMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("HH");
		DateFormat dateFormat3 = new SimpleDateFormat("mmss");

		TimeZone timeZone = TimeZone.getTimeZone("GMT+07");
		dateFormat1.setTimeZone(timeZone);
		dateFormat2.setTimeZone(timeZone);
		dateFormat3.setTimeZone(timeZone);

		Date newDate = new Date();
		String bit7 = dateFormat1.format(newDate) + dateFormat2.format(newDate) + dateFormat3.format(newDate);
		String bit12 = dateFormat2.format(newDate) + dateFormat3.format(newDate);
		String bit13 = dateFormat1.format(newDate);
		String bit15 = Integer.toString(Integer.parseInt(bit13) + 1);

		HashMap<String, String> result = new HashMap<String, String>();

		result.put("bit7", bit7);
		result.put("bit12", bit12);
		result.put("bit13", bit13);
		result.put("bit15", bit15);

		return result;
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
		System.out.println("request : " + new String(messageBody));

		short messageLength = (short) messageBody.length;

		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short) messageLength);
		byte[] header = bb.array();

		return concat(header, messageBody);
	}

	private void sendNetworkMessage() {
		System.out.println("send network message");
		// if (mti.length() == 0) {
		// return;
		// }

		// Logger logger = new Logger();
		// logger.addListener(new SimpleLogListener(System.out));

		Map<String, String> date = getDate();
		ISOMsg reply = null;
		try {
			// ((LogSource) channel).setLogger(logger, "test-channel");

			System.out.println("trying to connect...");
			channel.connect();
			System.out.println("connected...");

			ISOMsg msg = new ISOMsg();

			msg.setMTI("0800");
			msg.set(7, date.get("bit7"));
			msg.set(11, "820479");
			msg.set(70, "001");
			msg.setPackager(new ISO87APackager());

			byte[] msgByte = createMessageAJ(msg);

			channel.send(msgByte);
			reply = channel.receive();
			if (reply == null) {
				return;
			}

			System.out.println("response: " + new String(reply.pack()));
			logISOMsg(reply);
			System.out.println();

			ISOMsg msg2 = new ISOMsg();

			msg2.setMTI("0810");
			msg2.set(7, date.get("bit7"));
			msg2.set(11, "820480");

			msg2.set(39, "00");
			msg2.set(70, "001");
			msg2.setPackager(new ISO87APackager());

			byte[] msgByte2 = createMessageAJ(msg2);

			channel.send(msgByte2);
			reply = channel.receive();
			if (reply == null) {
				return;
			}

			System.out.println("response: " + new String(reply.pack()));
			logISOMsg(reply);
			System.out.println();

			// sendInquiryPostpaid(channel);

		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// try {
			// channel.disconnect();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
	}

	private void sendInquiryPostpaid(ISOChannel channel) {
		Map<String, String> date = getDate();

		// ISOChannel channel = new AJChannel(host, port, new ISO87APackager());

		ISOMsg reply = null;
		try {
			// channel.connect();
			ISOMsg msg = new ISOMsg();
			msg.setMTI("0200");
			// msg.set(1, "723A400128618002");
			msg.set(2, "454633334444");
			msg.set(3, "380000");
			msg.set(4, "000000000000");
			msg.set(7, date.get("bit7"));

			msg.set(11, "820475"); // trx_id?

			msg.set(12, date.get("bit12"));
			msg.set(13, date.get("bit13"));
			msg.set(15, date.get("bit15"));
			msg.set(18, "6021");
			msg.set(32, "000735");
			msg.set(35, "454633334444=;=0909");
			msg.set(37, "160664820475"); // trx_id?
			msg.set(42, "9999           ");
			// msg.set(42, "AXS9999        ");
			msg.set(43, "AXES                                    ");
			// msg.set(48, "2112131234561111 "); // postpaid
			msg.set(48, "21144567891230123000"); // nontaglis
			msg.set(49, "360");
			msg.set(63, "214");
			msg.setPackager(new ISO87APackager());

			byte[] msgByte = createMessageAJ(msg);

			channel.send(msgByte);

			reply = channel.receive();
			if (reply == null) {
				return;
			}

			byte[] replyByte = reply.pack();
			System.out.println("response: " + new String(replyByte));
			logISOMsg(reply);

			sendPaymentPostpaid(channel, reply);

		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPaymentPostpaid(ISOChannel channel, ISOMsg responseInquiry) {
		Map<String, String> date = getDate();

		// ISOChannel channel = new AJChannel(host, port, new ISO87APackager());
		String bit48 = responseInquiry.getString(48);
		String resBit48 = bit48.substring(0, 18) + bit48.charAt(18) + bit48.substring(18, bit48.length());
		System.out.println("new bit48: " + resBit48);
		ISOMsg reply = null;
		try {
			// channel.connect();
			ISOMsg msg = new ISOMsg();
			msg.setMTI("0200");
			// msg.set(1, "723A400128618002");
			msg.set(2, "454633334444");
			msg.set(3, "180000");
			msg.set(4, "000000050000");
			msg.set(7, date.get("bit7"));
			msg.set(11, "820476"); // trx_id?
			msg.set(12, date.get("bit12"));
			msg.set(13, date.get("bit13"));
			msg.set(15, date.get("bit15"));
			msg.set(18, "6021");
			msg.set(32, "000735");
			msg.set(35, "454633334444=;=0909");
			msg.set(37, "160664820476"); // trx_id?
			msg.set(42, "9999           ");
			msg.set(43, "AXES                                    ");
			// msg.set(48, bit48.substring(0, 18) + bit48); // postpaid
			// msg.set(48, "2112131234561111 " + responseInquiry.getString(48));
			msg.set(48, resBit48);
			// msg.set(48, "21144567891230123000"); // nontaglis

			msg.set(49, "360");
			msg.set(63, "214");
			msg.setPackager(new ISO87APackager());

			logISOMsg(msg);
			byte[] msgByte = createMessageAJ(msg);

			channel.send(msgByte);

			reply = channel.receive();
			if (reply == null) {
				return;
			}

			byte[] replyByte = reply.pack();
			System.out.println("response: " + new String(replyByte));
			logISOMsg(reply);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendInquiryPrepaid(ISOChannel channel) {
		Map<String, String> date = getDate();

		// ISOChannel channel = new AJChannel(host, port, new ISO87APackager());

		ISOMsg reply = null;
		try {
			// channel.connect();
			ISOMsg msg = new ISOMsg();
			msg.setMTI("0200");
			// msg.set(1, "723A400128618002");
			msg.set(2, "454633334444");
			msg.set(3, "380000");
			msg.set(4, "000000000000");
			msg.set(7, date.get("bit7"));
			msg.set(11, "820476");
			msg.set(12, date.get("bit12"));
			msg.set(13, date.get("bit13"));
			msg.set(15, date.get("bit15"));
			msg.set(18, "6021");
			msg.set(32, "000735");
			msg.set(37, "160664820475");
			msg.set(42, "AXS9999        ");
			msg.set(43, "AXES                                    ");
			msg.set(48, "211112345673221             0");
			msg.set(49, "360");
			msg.set(63, "214");
			msg.setPackager(new ISO87APackager());

			byte[] msgByte = createMessageAJ(msg);

			channel.send(msgByte);

			reply = channel.receive();
			if (reply == null) {
				return;
			}

			byte[] replyByte = reply.pack();
			System.out.println("response: " + new String(replyByte));
			bit48 = reply.getValue(48).toString();
			bit39 = reply.getValue(39).toString();
			logISOMsg(reply);
			// if (bit48.length()>17 && reply.getValue(39).equals("00")) {
			//
			// sendPaymentPostpaid(channel, bit48);
			// }
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPaymentPostpaid(ISOChannel channel, String bit48) {
		Map<String, String> date = getDate();

		// ISOChannel channel = new AJChannel(host, port, new ISO87APackager());

		ISOMsg reply = null;
		try {
			// channel.connect();
			ISOMsg msg = new ISOMsg();
			msg.setMTI("0200");
			// msg.set(1, "723A400128618002");
			msg.set(2, "454633334444");
			msg.set(3, "180000");
			msg.set(4, "000000200000");
			msg.set(7, date.get("bit7"));
			msg.set(11, "820705"); // postpaid
			// msg.set(11, "890931"); // prepaid
			msg.set(12, date.get("bit12"));
			msg.set(13, date.get("bit13"));
			msg.set(15, date.get("bit15"));
			msg.set(18, "6021");
			msg.set(32, "000735");
			msg.set(35, "454633334444=;=0909");
			msg.set(37, "000000891963");
			// msg.set(37, "160664820475");
			msg.set(42, "9999           ");
			msg.set(43, "AXES                                    ");
			// msg.set(48, "21144567891230123000"); // nontaglist
			// msg.set(48,
			// "21111234567890 131234561111 0AZHAR WAHYU' S.T,M-T     R1  000002200DCSUNITSUPHONE123     1000000000000000000000000000KGIM9AVVVVVV3XVZ21XVWZWXWWW0Y0YX073521                          0P");
			// // payment prepaid
			// msg.set(48, "2112131234561111 "); // postpaid
			msg.set(48, bit48.substring(0, 18) + "" + bit48.substring(17, 18) + "" + bit48.substring(18, 52) + "      "
					+ bit48.substring(58, bit48.length())); // payment postpaid

			msg.set(49, "360");
			msg.set(63, "214");
			msg.setPackager(new ISO87APackager());

			logISOMsg(msg);
			byte[] msgByte = createMessageAJ(msg);

			channel.send(msgByte);

			reply = channel.receive();
			if (reply == null) {
				return;
			}
			// channel.send(msgByte);

			reply = channel.receive();
			if (reply == null) {
				return;
			}
			byte[] replyByte = reply.pack();
			System.out.println("response: " + new String(replyByte));
			logISOMsg(reply);
		} catch (ISOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void logISOMsg(ISOMsg msg) {
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

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static void main(String[] args) {

		IsoClient client = new IsoClient();

		client.sendNetworkMessage();
		client.sendInquiryPostpaid(channel);
		if (bit48.length() > 17 && bit39.equals("00")) {
			client.sendPaymentPostpaid(channel, bit48);
		} else {
			System.out.println("payment fail");
			System.out.println("bit48=" + bit48);
			System.out.println("bit39=" + bit39);
		}
		// client.sendNetworkMessage("0810");

		// client.sendInquiryPostpaid("2112131234561111 ");

	}
}
