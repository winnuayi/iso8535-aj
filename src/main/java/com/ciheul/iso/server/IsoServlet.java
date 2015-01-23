// In [86]: headers = {'Content-type': 'application/json', 'Accept': 'application/json'}
// In [87]: requests.post('http://localhost:8080/hello/send', data=json.dumps(payload), headers=headers).text
// Out[87]: u'{"message":"ciheul"}'

package com.ciheul.iso.server;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class IsoServlet {

    @POST
    @Path("/send")
    @Produces(MediaType.APPLICATION_JSON)
    public IsoMessageResponse send(IsoMessageRequest msg) {
        IsoMessageResponse response = new IsoMessageResponse();
        response.setMessage("ciheul");
        System.out.println(response.toString());
        return response;
    }

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public IsoMessageResponse send2(IsoMessageRequest msg) {
        IsoMessageResponse response = new IsoMessageResponse();
        response.setMessage("ciheul");
        System.out.println(response.toString());
        return response;
    }

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_HTML)
    public IsoMessageResponse send3(IsoMessageRequest msg) {
        IsoMessageResponse response = new IsoMessageResponse();
        response.setMessage("ciheul");
        System.out.println(response.toString());
        return response;
    }

    @GET
    @Path("/send")
    @Produces(MediaType.APPLICATION_JSON)
    public IsoMessageResponse sayHello() {
        IsoMessageResponse response = new IsoMessageResponse();
        response.setMessage("ciheul");
        System.out.println("ciheeeeul!");
        return response;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayPlainTextHello() {
        return "Hello Jersey";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Jersey" + "</title>" + "<body><h1>" + "Hello Jersey" + "</body></h1>"
                + "</html> ";
    }

}
