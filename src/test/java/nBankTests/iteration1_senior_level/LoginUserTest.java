package nBankTests.iteration1_senior_level;

import generatos.RandomModelGenerator;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.loginUser.LoginUserRequest;
import nBankTests.BaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponceSpecs;
import utils.UserData;

public class LoginUserTest extends BaseTest {
    private static UserData user;

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponceSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponceSpecs.entityWasCreated())
                .post(userRequest);
        user = UserData.createFrom(createUserResponse);

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponceSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .header("Authorization", Matchers.notNullValue());
    }

    @AfterAll
    public static void deleteTestData() {
        AdminSteps.deleteUser(user);
    }
}
