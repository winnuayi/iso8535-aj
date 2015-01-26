// In [85]: headers = { 'Content-type': 'application/json', 'Accept': 'application/json' }
// In [86]: payload = { 'message': 'hello world!' }
// In [87]: requests.post('http://localhost:9092/api/send', data=json.dumps(payload), headers=headers).text
// Out[87]: u'{"message":"ciheul"}'

package com.ciheul.iso.server;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

@Path("/api")
public class IsoServlet {

    ChannelManager channelManager = ChannelManager.getInstance();

    @POST
    @Path("/send")
    @Produces(MediaType.APPLICATION_JSON)
    public IsoMessageResponse send(IsoMessageRequest msg){        
        String responseMsg = "";
        ISOMsg resp = null;
        
        System.out.println("request:" + msg.getMessage());
        
        String[] isoMsgSplit = msg.getMessage().split("#");
        
        channelConnection();
        
        String isoMsgSend = msg.getMessage();
        
        // String isoMsgSend = msg.getMessage().substring(5,
        // msg.getMessage().length());
        
        switch (isoMsgSplit[0]) {
        case "0800":
            try {
                resp = channelManager.sendMsg(createHandshakeISOMsg(isoMsgSend));
                if (resp != null) {

                    responseMsg = resp.getValue(39).toString();
                } 
//                channelManager.getLog().info("Handshake sent! ");
            } catch (ISOException e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            }
            break;

        case "0810":
            try {
                resp = channelManager.sendMsg(createHandshakeISOMsg2(isoMsgSend));
                if (resp != null) {
                    responseMsg = resp.getValue(39).toString();
                }
//                channelManager.getLog().info("Handshake sent! ");
            } catch (ISOException e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            }
            break;

        case "0200":
            try {
                resp = channelManager.sendMsg(createSendInquiryISOMsg(isoMsgSend));
                System.out.println("sent");
                System.out.println("Loggernya = "+channelManager.getLogger());
                if (resp != null) {
                    responseMsg = resp.getValue(4).toString() + "#" + resp.getValue(39).toString() + "#"
                            + resp.getValue(48);
                }else {
                	responseMsg = "TIMEOUT";
				}
//                channelManager.getLog().info("Handshake sent! ");
            } catch (ISOException e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            }
            break;

        case "0210":
            System.out.println("masuk sini jugaa");
            break;

        case "0400":
            try {
                resp = channelManager.sendMsg(createSendReversalISOMsg(isoMsgSend));
                if (resp != null) {
                    responseMsg = resp.getValue(39).toString();
                }else{
                	responseMsg = "TIMEOUT";
                }
//                channelManager.getLog().info("Handshake sent! ");
            } catch (ISOException e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
            	responseMsg = e.getMessage();
                e.printStackTrace();
            }
            break;

        case "0410":
            System.out.println("sini juga masuk");
            break;

        default:
            System.out.println("not found");
            break;
        }
        IsoMessageResponse response = new IsoMessageResponse();
        response.setMessage(responseMsg);
        System.out.println("response: " + response.getMessage());
        return response;
    }

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
    private ISOMsg createSendInquiryISOMsg(String isoMsgSend) throws ISOException {
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
        m.setPackager(new ISO87APackager());
        ChannelManager.logISOMsg(m);
        return m;
    }

    private void channelConnection() {
        try {
            channelManager = ((ChannelManager) NameRegistrar.get("manager"));
        } catch (NameRegistrar.NotFoundException e) {
            LogEvent evt = channelManager.getLog().createError();
            evt.addMessage(e);
            evt.addMessage(NameRegistrar.getInstance());
            Logger.log(evt);
        } catch (Throwable t) {
            channelManager.getLog().error(t);
        }
    }
    // @POST
    // @Path("/send")
    // @Produces(MediaType.TEXT_PLAIN)
    // public IsoMessageResponse send2(IsoMessageRequest msg) {
    // IsoMessageResponse response = new IsoMessageResponse();
    // response.setMessage("ciheul");
    // System.out.println(response.toString());
    // return response;
    // }
    //
    // @POST
    // @Path("/send")
    // @Produces(MediaType.TEXT_HTML)
    // public IsoMessageResponse send3(IsoMessageRequest msg) {
    // IsoMessageResponse response = new IsoMessageResponse();
    // response.setMessage("ciheul");
    // System.out.println(response.toString());
    // return response;
    // }

    // @GET
    // @Path("/send")
    // @Produces(MediaType.APPLICATION_JSON)
    // public IsoMessageResponse sayHello() {
    // IsoMessageResponse response = new IsoMessageResponse();
    // response.setMessage("ciheul");
    // System.out.println("ciheeeeul!");
    // return response;
    // }
    //
    // @GET
    // @Produces(MediaType.TEXT_PLAIN)
    // public String sayPlainTextHello() {
    // return "Hello Jersey";
    // }
    //
    // @GET
    // @Produces(MediaType.TEXT_HTML)
    // public String sayHtmlHello() {
    // return "<html> " + "<title>" + "Hello Jersey" + "</title>" + "<body><h1>"
    // + "Hello Jersey" + "</body></h1>"
    // + "</html> ";
    // }

}
