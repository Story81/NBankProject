package nBankTests;

import generatos.RandomData;
import io.restassured.http.ContentType;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequest;
import requests.AdminLoginUserRequest;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.deleteUser;
import static nBankTests.CommonSteps.deleteUsersAccount;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateAccountTest extends BaseTest {

    private static String username = "Kate_" + UUID.randomUUID().toString().substring(0, 8);
    private static String password = "Kate2000#";
    private static String role = "USER";
    private static String userAuthHeader;
    private static int accountId;
    private static String userId;

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .build();

        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();
        // создание пользователя

        new AdminCreateUserRequest(
                RequestSpecs.adminSpec(),
                ResponceSpecs.entityWasCreated())
                .post(userRequest);



        // получаем токен юзера
        String userAuthHeader = new AdminLoginUserRequest(
                RequestSpecs.unauthSpec(),
                ResponceSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .extract().header("Authorization");



        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password))
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("[0].id", notNullValue())
                .body("[0].accountNumber", notNullValue())
                .body("[0].balance", equalTo(0.0f))
                .body("[0].transactions", empty())
                .extract()
                .path("[0].id");
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteUsersAccount(userAuthHeader, accountId);
        deleteUser(ADMIN_AUTH, userId);
    }

}
