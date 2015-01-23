package jersey.client;

import java.net.URI;
import java.text.Normalizer.Form;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

    public static void main(String[] args) {

    	
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

    private static URI getBaseURI() {

        return UriBuilder.fromUri("http://localhost:8080/hello/send").build();

    }
}
