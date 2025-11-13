package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import requests.GetRequest;

import static io.restassured.RestAssured.given;

public class GetCustomerProfileRequestSender extends GetRequest {
    public GetCustomerProfileRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/customer/profile")
                .then()
                .spec(responseSpec);
    }
}
