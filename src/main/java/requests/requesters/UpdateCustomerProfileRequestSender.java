package requests.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.customer.UpdateCustomerProfileRequest;
import requests.PutRequest;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileRequestSender extends PutRequest<UpdateCustomerProfileRequest>{
    public UpdateCustomerProfileRequestSender(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    @Override
    public ValidatableResponse put(UpdateCustomerProfileRequest model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpec);
    }
}
