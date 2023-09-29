package com.github.azthec;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class TodoResourceTest {

    @TestHTTPResource
    String apiRoot;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = apiRoot + "todo";
    }

    @Test
    void testApiRootRespondsToGet() {
        given()
            .when().get()
            .then().statusCode(200);
    }

    @Test
    void testApiRootRespondsToPost() {
        given()
            .body("{\"title\":\"a todo\"}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .when().post()
            .then().statusCode(200).body("title", equalTo("a todo"));
    }

    @Test
    void testApiRootRespondsToDelete() {
        given()
            .when().delete()
            .then().statusCode(204);
    }

    @Test
    void testApiRootAfterDelete() {
        given()
            .when().delete()
            .then().statusCode(200).body(isEmptyOrNullString());
    }

    @Test
    void testAddingNewTodoToRootUrl() {
        given()
            .when().delete().then().statusCode(204); // Ensure root is empty before adding a new todo

        given()
            .body("{\"title\":\"walk the dog\"}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .when().post()
            .then().statusCode(200);

        given()
            .when().get()
            .then().statusCode(200).body("size()", equalTo(1))
            .body("[0].title", equalTo("walk the dog"));
    }

    @Test
    void testSetupNewTodoAsNotCompleted() {
        given()
            .when().delete().then().statusCode(204); // Ensure root is empty before adding a new todo

        given()
            .body("{\"title\":\"blah\"}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .when().post()
            .then().statusCode(200)
            .body("completed", equalTo(false));
    }

    @Test
    void testEachNewTodoHasUrl() {
        given()
            .when().delete().then().statusCode(204); // Ensure root is empty before adding a new todo

        given()
            .body("{\"title\":\"blah\"}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .when().post()
            .then().statusCode(200)
            .body("url", notNullValue());
    }

    @Test
    void testEachNewTodoHasUrlAndReturnsTodo() {
        given()
            .body("{\"title\":\"my todo\"}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .when().post()
            .then().statusCode(200)
            .body("url", notNullValue())
            .body("title", equalTo("my todo"));

        // Fetch the created todo
        given()
            .when().get()
            .then().statusCode(200).body("size()", equalTo(1))
            .body("[0].title", equalTo("my todo"));
    }
    @Test
    void testWorkingWithExistingTodo() {
        // Clear existing todos before each test
        given().when().delete().then().statusCode(204);

        // Test navigating from a list of todos to an individual todo via URLs
        given()
                .body("{\"title\":\"todo the first\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);
        given()
                .body("{\"title\":\"todo the second\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        given()
                .when().get()
                .then().statusCode(200)
                .body("size()", equalTo(2));

        given()
                .when().get().then().statusCode(200)
                .body("[0].title", equalTo("todo the first"));

        // Test changing the todo's title by PATCHing to the todo's URL
        given()
                .body("{\"title\":\"initial title\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String urlForNewTodo = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .body("{\"title\":\"bathe the cat\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().patch(urlForNewTodo)
                .then().statusCode(200)
                .body("title", equalTo("bathe the cat"));

        // Test changing the todo's completedness by PATCHing to the todo's URL
        given()
                .body("{\"title\":\"test todo\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String urlForTodoToComplete = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .body("{\"completed\":true}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().patch(urlForTodoToComplete)
                .then().statusCode(200)
                .body("completed", equalTo(true));

        // Test changes to a todo are persisted and show up when re-fetching the todo
        given()
                .body("{\"title\":\"changed title\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String urlForChangedTodo = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .body("{\"title\":\"changed title\", \"completed\":true}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().patch(urlForChangedTodo)
                .then().statusCode(200);

        given()
                .when().get(urlForChangedTodo)
                .then().statusCode(200)
                .body("title", equalTo("changed title"))
                .body("completed", equalTo(true));

        // Test deleting a todo by making a DELETE request to the todo's URL
        given()
                .body("{\"title\":\"delete me\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String urlForTodoToDelete = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .when().delete(urlForTodoToDelete)
                .then().statusCode(204);

        given()
                .when().get()
                .then().statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testTrackingTodoOrder() {
        // Test creating a todo with an order field
        given()
                .body("{\"title\":\"blah\",\"order\":523}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200)
                .body("order", equalTo(523));

        // Test PATCHing a todo to change its order
        given()
                .body("{\"title\":\"initial title\",\"order\":10}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String newTodoUrl = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .body("{\"order\":95}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().patch(newTodoUrl)
                .then().statusCode(200)
                .body("order", equalTo(95));

        // Test remembering changes to a todo's order
        given()
                .body("{\"title\":\"order test\",\"order\":10}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post()
                .then().statusCode(200);

        String newTodoUrlForOrderTest = given()
                .when().get().jsonPath().getString("[0].url");

        given()
                .body("{\"order\":95}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().patch(newTodoUrlForOrderTest)
                .then().statusCode(200);

        given()
                .when().get(newTodoUrlForOrderTest)
                .then().statusCode(200)
                .body("order", equalTo(95));
    }

}

