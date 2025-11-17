package nBankTests.iteration_2;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import nBankTests.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.deleteAllUsers;
import static nBankTests.CommonSteps.generateUsername;
import static nBankTests.CommonSteps.getAuthTokenTest;
import static nBankTests.CommonSteps.getUsers;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChangeUsersNameTest extends BaseTest {
    private static String password = "Kate2000#";
    private static String role = "USER";
    private static String userAuthHeader;
    private static String userId;
    private static List<String> createdUserIds = new ArrayList<>();
    private static String expiredAuthToken = "00F0ZV00NnEzMjVhNDpLYXRlM=";

    @Test
    public void userCanUpdateName() {
        String username = generateUsername();
        userId = createUser(username, password, role);
        createdUserIds.add(userId);
        userAuthHeader = getAuthTokenTest(username, password);
        String newName = "Kate Samsonova";

        updateUserName(newName, userAuthHeader, 200)
                .then()
                .assertThat()
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.id", equalTo(Integer.parseInt(userId)))
                .body("customer.password", not(equalTo(password)))
                .body("customer.name", equalTo(newName))
                .body("customer.role", equalTo(role))
                .body("customer.accounts", empty());

        getUsers(userAuthHeader, HttpStatus.SC_OK)
                .then()
                .assertThat()
                .body("id", equalTo(Integer.parseInt(userId)))
                .body("password", not(equalTo(password)))
                .body("username", equalTo(username))
                .body("name", equalTo(newName))
                .body("role", equalTo(role))
                .body("accounts", empty());
    }

    public static Stream<Arguments> changeNameInvalidData() {
        return Stream.of(
                Arguments.of("Mark1999 dfd dfd", "Name must contain two words with letters only"),
                Arguments.of("Mark1999 Samsonov", "Name must contain two words with letters only"),
                Arguments.of("Mark_Samsonov", "Name must contain two words with letters only"),
                Arguments.of("'   '", "Name must contain two words with letters only"),
                Arguments.of("Mark Junior Samsonov", "Name must contain two words with letters only")
        );
    }

    @ParameterizedTest
    @MethodSource("changeNameInvalidData")
    public void userCanNotUpdateName(String newName, String errorMassage) {
        String username = generateUsername();
        userId = createUser(username, password, role);
        createdUserIds.add(userId);
        userAuthHeader = getAuthTokenTest(username, password);

        updateUserName(newName, userAuthHeader, 400)
                .then()
                .assertThat()
                .body(equalTo(errorMassage));

        getUsers(userAuthHeader, HttpStatus.SC_OK)
                .then()
                .assertThat()
                .body("id", equalTo(Integer.parseInt(userId)))
                .body("password", not(equalTo(password)))
                .body("username", equalTo(username))
                .body("name", equalTo(null))
                .body("role", equalTo(role))
                .body("accounts", empty());
    }

    @Test
    public void userCannotUpdateNameWithInvalidToken() {
        String username = generateUsername();
        userId = createUser(username, password, role);
        createdUserIds.add(userId);
        userAuthHeader = getAuthTokenTest(username, password);
        String newName = "Kate Samsonova";

        updateUserName(newName, expiredAuthToken, 401)
                .then()
                .assertThat()
                .body(emptyString()); //тут должно быть сообщение об ошибке, а не просто пустое тело

        getUsers(userAuthHeader, HttpStatus.SC_OK)
                .then()
                .assertThat()
                .body("id", equalTo(Integer.parseInt(userId)))
                .body("password", not(equalTo(password)))
                .body("username", equalTo(username))
                .body("name", equalTo(null))
                .body("role", equalTo(role))
                .body("accounts", empty());
    }

    @Test
    public void userCannotUpdateNameWithoutToken() {
        String username = generateUsername();
        userId = createUser(username, password, role);
        createdUserIds.add(userId);
        userAuthHeader = getAuthTokenTest(username, password);
        String newName = "Kate Samsonova";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateUserName(newName, null, 401));
        assertEquals("Header value cannot be null", exception.getMessage());

        getUsers(userAuthHeader, HttpStatus.SC_OK)
                .then()
                .assertThat()
                .body("id", equalTo(Integer.parseInt(userId)))
                .body("password", not(equalTo(password)))
                .body("username", equalTo(username))
                .body("name", equalTo(null))
                .body("role", equalTo(role))
                .body("accounts", empty());
    }

    public static Response updateUserName(String newName, String authHeader, int statusCode) {
        return
                given().contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .header("Authorization", authHeader)
                        .body("""
                                 {
                                "name": "%s"
                                 }
                                 """.formatted(newName))
                        .put("/api/v1/customer/profile")
                        .then()
                        .assertThat()
                        .statusCode(statusCode)
                        .extract()
                        .response();
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteAllUsers(createdUserIds);
    }
}
