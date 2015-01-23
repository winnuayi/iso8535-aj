package jersey.client;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.ciheul.iso.server.IsoMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        IsoMessageRequest request = new IsoMessageRequest();
        request.setMessage("yoooo");

        String jsonString = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonString = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String responsePost = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonString), String.class);
        System.out.println(responsePost);

        String responseGet = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println(responseGet);
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/hello/send").build();
    }
}
