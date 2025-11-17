package nBankTests.iteration_1;

import io.restassured.http.ContentType;
import nBankTests.BaseTest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.deleteUser;

public class LoginUserTest extends BaseTest {
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
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", ADMIN_AUTH);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {

        given()
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
                .header("Authorization", Matchers.notNullValue());
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteUser(ADMIN_AUTH, userId);
    }
}