package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.loginUser.LoginUserRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;


public class LoginUserRequestSender extends PostRequest<LoginUserRequest> {
    public LoginUserRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }


    @Override
    public ValidatableResponse post(LoginUserRequest model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
