package api.requests.skeleton.requesters;

import api.requests.skeleton.interfaces.ICrudEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.IGetAllEndpoint;

import java.util.Arrays;
import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements ICrudEndpoint, IGetAllEndpoint {
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

    @Override
    public List<T> getAll(Class<?> clazz) {
        T[] array = (T[]) crudRequester
                .getAll(clazz)
                .extract().as(clazz);
        return Arrays.asList(array);
    }
}
