package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import requests.GetRequest;

import static io.restassured.RestAssured.given;

public class GetAccountsRequestSender extends GetRequest {
    public GetAccountsRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .spec(responseSpec);
    }
}
