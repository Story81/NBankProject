package requests.steps;

import generatos.RandomModelGenerator;
import io.restassured.response.Response;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponceSpecs;
import utils.UserData;

import java.util.List;

public class AdminSteps {
    public static UserData createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CrudRequester requester = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponceSpecs.entityWasCreated()
        );

        Response rawResponse = requester.postRaw(userRequest);
        rawResponse
                .then()
                .spec(ResponceSpecs.entityWasCreated());       //проверяем статус 201 у "сырого" ответа
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
                ResponceSpecs.requestReturnsOK())
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
}
