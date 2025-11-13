package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.CreateUserRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequest extends Request<CreateUserRequest>{
    public AdminCreateUserRequest(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
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
