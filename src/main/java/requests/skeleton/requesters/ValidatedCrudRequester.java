package requests.skeleton.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.ICrudEndpoint;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements ICrudEndpoint {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpec, Endpoint endpoint, ResponseSpecification responseSpec) {
        super(requestSpec, endpoint, responseSpec);
        this.crudRequester = new CrudRequester(requestSpec, endpoint, responseSpec);
    }

    public T post(BaseModel model) {
        return (T) crudRequester
                .post(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T get() {
        return (T) crudRequester
                .get()
                .extract().as(endpoint.getResponseModel());
    }

    @Override
    public T put(BaseModel model) {
        return (T) crudRequester
                .put(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T delete(int id) {
        return (T) crudRequester
                .delete(id)
                .extract()
                .as(endpoint.getResponseModel());
    }
}
