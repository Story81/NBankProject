package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import specs.BaseRequest;

public abstract class GetRequest extends BaseRequest {
    public GetRequest(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        super(requestSpec, responseSpec);
    }

    public abstract ValidatableResponse get();
}
