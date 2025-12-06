package api.requests.skeleton.requesters;

import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.ICrudEndpoint;
import api.requests.skeleton.interfaces.IGetAllEndpoint;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements ICrudEndpoint, IGetAllEndpoint {
    public CrudRequester(RequestSpecification requestSpec, Endpoint endpoint, ResponseSpecification responseSpec) {
        super(requestSpec, endpoint, responseSpec);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpec)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpec);
    }

    public Response postRaw(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpec)
                .body(body)
                .post(endpoint.getUrl());
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpec)
                .get(endpoint.getUrl())
                .then()
                .spec(responseSpec);
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return given()
                .spec(requestSpec)
                .body(model)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpec);
    }

    @Override
    public ValidatableResponse delete(int id) {
        return given()
                .spec(requestSpec)
                .pathParam("id", id)
                .when()
                .delete(endpoint.getUrl())
                .then()
                .spec(responseSpec);
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpec)
                .get(endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpec);
    }
}
