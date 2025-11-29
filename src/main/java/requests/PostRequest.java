package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import specs.BaseRequest;

public abstract class PostRequest<T extends BaseModel> extends BaseRequest {
    public PostRequest(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }
    public abstract ValidatableResponse post(T model);
}
