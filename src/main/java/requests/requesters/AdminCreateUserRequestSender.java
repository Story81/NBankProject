package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.admin.CreateUserRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequestSender extends PostRequest<CreateUserRequest> {
    public AdminCreateUserRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse post(CreateUserRequest model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
