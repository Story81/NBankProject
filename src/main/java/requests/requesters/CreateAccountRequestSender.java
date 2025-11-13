package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.accounts.CreateAccountRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;

public class CreateAccountRequestSender extends PostRequest<CreateAccountRequest>{
    public CreateAccountRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }


    @Override
    public ValidatableResponse post(CreateAccountRequest model) {
        return given()
                .spec(requestSpec)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
