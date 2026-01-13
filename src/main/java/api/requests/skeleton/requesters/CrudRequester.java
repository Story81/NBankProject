package api.requests.skeleton.requesters;

import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.ICrudEndpoint;
import api.requests.skeleton.interfaces.IGetAllEndpoint;
import common.helpers.StepLogger;
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
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpec)
                    .body(body)
                    .post(endpoint.getUrl())
                    .then()
                    .spec(responseSpec);
        });
    }

    public Response postRaw(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpec)
                    .body(body)
                    .post(endpoint.getUrl());
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .get(endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return StepLogger.log("PUT request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .body(model)
                        .put(endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }

    @Override
    public ValidatableResponse delete(int id) {
        return StepLogger.log("DELETE request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .pathParam("id", id)
                        .when()
                        .delete(endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .get(endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }
}
