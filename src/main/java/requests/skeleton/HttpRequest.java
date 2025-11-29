package requests.skeleton;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class HttpRequest {
    protected RequestSpecification requestSpec;
    protected Endpoint endpoint;
    protected ResponseSpecification responseSpec;

    public HttpRequest(RequestSpecification requestSpec, Endpoint endpoint, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.endpoint = endpoint;
        this.responseSpec = responseSpec;
    }
}
