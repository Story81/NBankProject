package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request <T extends BaseModel>{
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    public Request(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public abstract ValidatableResponse post(T model);
}
