package specs;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class BaseRequest<T extends BaseModel>{
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    public BaseRequest(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }
}
