package org.crypthing.things;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class QuarkusServiceTest
{
	@Test
	public void testServiceSuccess()
	{
		given().param("type", "2")
			.when().get("/service")
			.then()
				.statusCode(200)
				.body(is("Operação bem sucedida"));
	}
	@Test
	public void testServiceWarning()
	{
		given().param("type", "1")
			.when().get("/service")
			.then()
				.statusCode(400)
				.body(is("Falha na validação do request"));
	}
	@Test
	public void testServiceError()
	{
		given().param("type", "0")
			.when().get("/service")
			.then()
				.statusCode(500)
				.body(is("Ocorreu um erro na execução"));
	}
}