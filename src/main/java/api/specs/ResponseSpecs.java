package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class ResponseSpecs {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseSpecBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsOKWithMessage(String expectedMessage) {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("message", equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, List<String> errorValues) {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, containsInAnyOrder(errorValues.toArray()))
                .build();
    }

    public static ResponseSpecification requestReturns400WithoutKeyValue(String errorValue) {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(equalTo(errorValue))
                .build();
    }

    public static ResponseSpecification requestReturnsUnauthorized() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }

    public static ResponseSpecification requestReturnsForbidden(String errorValue) {
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(equalTo(errorValue))
                .build();
    }

    public static ResponseSpecification requestReturnsInternalServerError() {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .build();
    }
}
