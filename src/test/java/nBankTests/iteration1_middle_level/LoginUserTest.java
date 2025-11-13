package nBankTests.iteration1_middle_level;

import generatos.RandomData;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.loginUser.LoginUserRequest;
import nBankTests.BaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.requesters.AdminCreateUserRequestSender;
import requests.requesters.LoginUserRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import static models.UserRole.USER;
import static nBankTests.CommonSteps.deleteUser;

public class LoginUserTest extends BaseTest {
    private static String userId;

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new LoginUserRequestSender(RequestSpecs.unauthSpec(), ResponceSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequestSender(
                RequestSpecs.adminSpec(),
                ResponceSpecs.entityWasCreated())
                .post(userRequest)
                .extract().as(CreateUserResponse.class);
        userId = String.valueOf(createUserResponse.getId());

    new LoginUserRequestSender(RequestSpecs.unauthSpec(),
                ResponceSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .header("Authorization", Matchers.notNullValue());
    }

    @AfterAll
    public static void deleteTestData() {
        deleteUser(ADMIN_AUTH, userId);
    }
}
