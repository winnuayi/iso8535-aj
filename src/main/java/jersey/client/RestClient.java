package jersey.client;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class RestClient {

    public static void main(String[] args) {

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());

        System.out.println(target.path("hello").path("send").request()
                .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, form));
        
//        System.out.println(target.path("hello").path("send").request()
//                .accept(MediaType.APPLICATION_JSON).get(String.class));
        
    }

    private static URI getBaseURI() {

        return UriBuilder.fromUri("http://localhost:8080/").build();

    }
}
