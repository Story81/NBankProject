package api.requests.skeleton.requesters;

import api.configs.Config;
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
    private final static String API_VERSION = Config.getProperty("apiVersion");

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
                    .post(API_VERSION + endpoint.getUrl())
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
                    .post(API_VERSION + endpoint.getUrl());
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .get(API_VERSION + endpoint.getUrl())
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
                        .put(API_VERSION + endpoint.getUrl())
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
                        .delete(API_VERSION + endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () ->
                given()
                        .spec(requestSpec)
                        .get(API_VERSION + endpoint.getUrl())
                        .then()
                        .spec(responseSpec)
        );
    }
}
