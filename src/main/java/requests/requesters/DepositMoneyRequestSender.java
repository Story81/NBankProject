package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.accounts.DepositMoneyRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;

public class DepositMoneyRequestSender extends PostRequest<DepositMoneyRequest> {
    public DepositMoneyRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse post(DepositMoneyRequest model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
