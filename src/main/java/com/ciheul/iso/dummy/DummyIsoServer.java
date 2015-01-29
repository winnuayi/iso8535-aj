package com.ciheul.iso.dummy;

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
    public static String REV_REQ = "0400";

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

    /**
     * Send echo test response.
     * 
     * @param source
     *            (Artajasa) channel
     * @param m
     *            message from client
     */
    private void sendEchoTestResponse(ISOSource source, ISOMsg m) {
        System.out.println("sendEchoResponse");
//        try {
//            Thread.sleep(10000); // 1000 milliseconds is one second.
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
        try {
            m.setResponseMTI();
            m.set(39, "00");
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
     * Send sign-on response.
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
     * Send inquiry postpaid response.
     * 
     * @param source
     *            (Artajasa) channel
     * @param m
     *            message from client
     */
    private void sendInquiryPostpaid(ISOSource source, ISOMsg m) {
        System.out.println("sendInquiryPostpaid");
        try {
            m.setResponseMTI();
            m.set(39, "00");
            // m.set(48,
            // "2112121234567890 101YZ012343210ZYXWXVW0VWX0W4X1XW342                                NAMA BIN NAMA                 12345021            R1  12345678900000250000000015000003602009012009021520090101000000150000D00000000000000001500000001000000010000000200000001000000020000000100000002000");

            m.set(48,
                    "2112131234561111 404KGIM9AVVVVVV3XVZ20XVWZWXWWV4W4W0                                AZHAR WAHYU' S.T,M-T          SU001021-98765432   R1  00000130000000000000000020000003602014012014012020140102000000050000D000001111100000011110000000000000010000000200000000000000000000000000000000002014022014022020140202000000050000D000002222200000022220000000000000010000000200000000000000000000000000000000002014032014032000000000000000050000D000003333300000033330000000000000010000000200000000000000000000000000000000002014042014032000000000000000050000D00000444440000004444000000000000001000000020000000000000000000000000000000000");

            m.setPackager(new ISO87APackager());
            source.send(m);
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send payment postpaid response.
     * 
     * @param source
     *            (Artajasa) channel
     * @param m
     *            message from client
     */
    private void sendPaymentPostpaid(ISOSource source, ISOMsg m) {
//        try {
//            Thread.sleep(15000); // 1000 milliseconds is one second.
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
        System.out.println("sendInquiryPostpaid");
        try {
            m.setResponseMTI();
            m.set(39, "00");
            // m.set(48,
            // "2112121234567890 101YZ012343210ZYXWXVW0VWX0W4X1XW342                                NAMA BIN NAMA                 12345021            R1  12345678900000250000000015000003602009012009021520090101000000150000D00000000000000001500000001000000010000000200000001000000020000000100000002000");

            m.set(48,
                    "2112131234561111 4404KGIM9AVVVVVV3XVZ20XVWZWXWWV4W4W0                                AZHAR WAHYU' S.T,M-T          SU001021-98765432   R1  00000130000000000000000020000003602014012014012020140102000000050000D000001111100000011110000000000000010000000200000000000000000000000000000000002014022014022020140202000000050000D000002222200000022220000000000000010000000200000000000000000000000000000000002014032014032000000000000000050000D000003333300000033330000000000000010000000200000000000000000000000000000000002014042014032000000000000000050000D00000444440000004444000000000000001000000020000000000000000000000000000000000");

            m.setPackager(new ISO87APackager());
            source.send(m);
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send inquiry postpaid response.
     * 
     * @param source
     *            (Artajasa) channel
     * @param m
     *            message from client
     */
    private void sendReversal(ISOSource source, ISOMsg m) {
        System.out.println("sendReversal");
        try {
            m.setResponseMTI();
            m.set(39, "00");
//            m.set(48,
//                    "21144567891230123000PENYAMBUNGAN BARU        2011062420111224531234563301 AZHAR WAHYU'B S.Pd,M-Pd       KGIM9AVVVVVV3XVZ20XVWZWXWWV4XXW1                                SU001JL. RAYA 1 BOJONGGEDE              021-66655544   00000200000003600000000000020000000000000000020000000000000000001200000000050000000");
            m.setPackager(new ISO87APackager());
            source.send(m);
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendInquiryNontaglis(ISOSource source, ISOMsg m) {
        System.out.println("sendInquiryPostpaid");
        try {
            m.setResponseMTI();
            m.set(39, "00");
            m.set(48,
                    "21144567891230123000PENYAMBUNGAN BARU        2011062420111224531234563301 AZHAR WAHYU'B S.Pd,M-Pd       KGIM9AVVVVVV3XVZ20XVWZWXWWV4XXW1                                SU001JL. RAYA 1 BOJONGGEDE              021-66655544   00000200000003600000000000020000000000000000020000000000000000001200000000050000000");
            m.setPackager(new ISO87APackager());
            source.send(m);
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send payment postpaid response.
     * 
     * @param source
     *            (Artajasa) channel
     * @param m
     *            message from client
     */
    private void sendPaymentNontaglis(ISOSource source, ISOMsg m) {
        System.out.println("sendInquiryPostpaid");
        try {
            m.setResponseMTI();
            m.set(39, "00");           
            m.set(48,
                    "21144567891230123000PENYAMBUNGAN BARU        2011062420111224531234563301 AZHAR WAHYU'B S.Pd,M-Pd       KGIM9AVVVVVV3XVZ20XVWZWXWWV4XXW1                                SU001JL. RAYA 1 BOJONGGEDE              021-66655544   00000200000003600000000000020000000000000000020000000000000000001200000000050000000");
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
        	if (m.getMTI().equals(REV_REQ)) {
//                sendReversal(source, m);
                return true;
            }else if (m.getMTI().equals(ECHO_REQ)) {
                sendEchoTestResponse(source, m);
                return true;
            } else if (m.getMTI().equals(ECHO_RES)) {
                sendSignOnResponse(source, m);
                return true;
            } else if (m.getMTI().equals(TRX_REQ)) {
                // POSTPAID INQ
                if (m.getValue(48).toString().substring(0, 4).equals("2112")) {
                    if (m.getValue(3).toString().equals("380000")) {
                        sendInquiryPostpaid(source, m);
                    } else if (m.getValue(3).toString().equals("180000")) {
                    	if (m.getValue(48).toString().substring(m.getValue(48).toString().length()-2, m.getValue(48).toString().length()-1).equals("A")) {

                          sendPaymentPostpaid(source, m);
						}
                    }                    
                } else if (m.getValue(48).toString().substring(0, 4).equals("2114")) {
                    if (m.getValue(3).toString().equals("380000")) {
                        sendInquiryNontaglis(source, m);
                    } else if (m.getValue(3).toString().equals("180000")) {
                        sendPaymentNontaglis(source, m);
                    }  
                }
                return true;
            } else {
                return false;
            }
        } catch (ISOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener(System.out));

        ServerChannel channel = new AJChannel(new ISO87APackager());
        ((LogSource) channel).setLogger(logger, "channel");

        ISOServer server = new ISOServer(2231, channel, null);
        server.setLogger(logger, "server");
        server.addISORequestListener(new DummyIsoServer());

        new Thread(server).start();
    }
}
