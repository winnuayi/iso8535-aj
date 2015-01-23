package jersey.client;

import java.net.URI;
import java.text.Normalizer.Form;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

//<<<<<<< HEAD
//=======
//import com.ciheul.iso.server.IsoMessageRequest;
//>>>>>>> 2a8edd87d4248cfddeeebd03a1cfe959973fba0c
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

    public static void main(String[] args) {
//<<<<<<< HEAD

    	
//    	IsoMsgRequest form = new IsoMsgRequest(1, "test");
    	ClientResponse form = new ClientResponse();
    	form.setMessage("tesss");
    	String jsonString = null;
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			jsonString = mapper.writeValueAsString(form);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//    	String input = "{\"singer\":\"Metallica\",\"title\":\"Fade To Black\"}";
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        
        
        System.out.println(target.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonString), String.class));
        
//        System.out.println(target.path("hello").path("send").request()
//                .accept(MediaType.APPLICATION_JSON).get(String.class));
        
    }
//=======
//        ClientConfig config = new ClientConfig();
//        Client client = ClientBuilder.newClient(config);
//        WebTarget target = client.target(getBaseURI());
//
//        IsoMessageRequest request = new IsoMessageRequest();
//        request.setMessage("yoooo");
//>>>>>>> 2a8edd87d4248cfddeeebd03a1cfe959973fba0c

//        String jsonString = null;
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            jsonString = mapper.writeValueAsString(request);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

//<<<<<<< HEAD
//        return UriBuilder.fromUri("http://localhost:8080/hello/send").build();
//=======
//        String responsePost = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
//                .post(Entity.json(jsonString), String.class);
//        System.out.println(responsePost);
//>>>>>>> 2a8edd87d4248cfddeeebd03a1cfe959973fba0c
//
//        String responseGet = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
//        System.out.println(responseGet);
//    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/hello/send").build();
    }
}
