package se.skltp.aggregatingservices.hawtioauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.aggregatingservices.AgpApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {AgpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hawtioauth")
public class HawtioAuthTests {

  @LocalServerPort
  private int serverPort;

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  public void redirectToLoginTest() throws Exception {
    String hawtio_url = String.format("http://localhost:%d/actuator/hawtio/", serverPort);
    String login_url = hawtio_url + "login";

    HttpRequest request = HttpRequest.newBuilder(new URI(hawtio_url)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(302, response.statusCode());
    assertTrue(response.headers().firstValue("location").isPresent());
    assertEquals(login_url, response.headers().firstValue("location").get());
  }

  @Test
  public void doLoginTest() throws Exception {
    URI uri = new URI(String.format("http://localhost:%d/actuator/hawtio/auth/login", serverPort));
    String body = "{ \"username\": \"testuser\", \"password\": \"test\" }";

    HttpRequest request = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertTrue(response.headers().firstValue("set-cookie").isPresent());
  }
}
