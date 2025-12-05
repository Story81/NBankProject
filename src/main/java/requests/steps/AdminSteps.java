package requests.steps;

import generatos.RandomModelGenerator;
import io.restassured.response.Response;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.UserData;

import java.util.List;

public class AdminSteps {
    public static UserData createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CrudRequester requester = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated()
        );

        Response rawResponse = requester.postRaw(userRequest);
        rawResponse
                .then()
                .spec(ResponseSpecs.entityWasCreated());       //проверяем статус 201 у "сырого" ответа
        CreateUserResponse userResponse = rawResponse.as(CreateUserResponse.class);

        return new UserData(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userResponse.getId(),
                rawResponse.getHeader("Authorization"),
                userResponse.getName(),
                userResponse.getRole()
        );
    }

    public static void deleteUser(UserData user) {
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE_USER,
                ResponseSpecs.requestReturnsOK())
                .delete(user.id());
    }

    public static void deleteAllUsers(List<UserData> createdUserIds) {
        for (UserData user : createdUserIds) {
            try {
                deleteUser(user);
            } catch (Exception e) {
                System.out.println("ALL USERS FOR DELETE:" + createdUserIds);
                System.out.println(e.getMessage());
            }
        }
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOK())
                .getAll(CreateUserResponse[].class);
    }
}
