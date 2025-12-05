package api.specs;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;

public abstract class BaseRequest<T extends BaseModel>{
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    public BaseRequest(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }
}
