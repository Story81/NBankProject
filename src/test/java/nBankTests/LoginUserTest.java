package nBankTests;
import generatos.RandomData;
import io.restassured.http.ContentType;
import models.LoginUserRequest;
import nBankTests.BaseTest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.AdminLoginUserRequest;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.deleteUser;

public class LoginUserTest extends BaseTest{
    private static String username = "Kate_" + UUID.randomUUID().toString().substring(0, 8);
    private static String password = "Kate2000#";
    private static String role = "USER";
    private static String userId;

    @BeforeAll
    public static void createTestData() {
        userId = createUser(username, password, role);
    }

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new AdminLoginUserRequest(RequestSpecs.unauthSpec(), ResponceSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
LoginUserRequest userRequest = LoginUserRequest.builder()
        .username(RandomData.getUserName())
        .password(RandomData.getPassword())
        .build();


        new AdminLoginUserRequest(RequestSpecs.unauthSpec(), ResponceSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteUser(ADMIN_AUTH, userId);
    }
}
