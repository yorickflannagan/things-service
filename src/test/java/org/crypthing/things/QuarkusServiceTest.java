package org.crypthing.things;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class QuarkusServiceTest {

    @Test
    public void testServiceEndpoint() {
        given().param("type", "2")
          .when().get("/service")
          .then()
             .statusCode(200)
             .body(is("Operação bem sucedida"));
    }

}