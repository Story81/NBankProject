package requests.requesters;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.accounts.DepositMoneyRequest;
import models.accounts.TransferMoneyRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;

public class TransferMoneyRequestSender extends PostRequest<TransferMoneyRequest> {

    public TransferMoneyRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse post(TransferMoneyRequest model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
