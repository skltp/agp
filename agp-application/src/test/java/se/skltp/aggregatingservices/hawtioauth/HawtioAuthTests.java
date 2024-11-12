package se.skltp.aggregatingservices.hawtioauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.aggregatingservices.AgpApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {AgpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hawtioauth")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HawtioAuthTests {

  @LocalServerPort
  private int serverPort;

  @Test
  public void doLoginTest() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    URI uri = new URI(String.format("http://localhost:%d/actuator/hawtio/auth/login", serverPort));
    String body = "{ \"username\": \"testuser\", \"password\": \"test\" }";

    HttpRequest request = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertTrue(response.headers().firstValue("set-cookie").isPresent());
  }

  @Test
  public void failedLoginTest() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    URI uri = new URI(String.format("http://localhost:%d/actuator/hawtio/auth/login", serverPort));
    String body = "{ \"username\": \"wronguser\", \"password\": \"anything\" }";

    HttpRequest request = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(403, response.statusCode());
    assertFalse(response.headers().firstValue("set-cookie").isPresent());
  }
}
