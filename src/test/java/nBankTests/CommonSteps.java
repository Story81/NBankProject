package nBankTests;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static nBankTests.BaseTest.ADMIN_AUTH;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;


public class CommonSteps {
    public static String generateUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static void deleteUsersAccount(String userAuthHeader, int accountId) {
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .delete("/api/v1/accounts/{accountId}", accountId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static void deleteUser(String userAuthHeader, String userId) {
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .delete("/api/v1/admin/users/{id}", userId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static void deleteAllUsers(List<String> createdUserIds) {
        for (String userId : createdUserIds) {
            try {
                deleteUser(ADMIN_AUTH, userId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String createUser(String username, String password, String role) {
        String userId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "%s"
                        }
                        """.formatted(username, password, role))
                .when()
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id").toString();
        return userId;
    }

    public static int createUsersAccount(String userAuthHeader) {
        int accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
        return accountId;
    }

    public static String getAuthTokenTest(String username, String password) {
        String authToken = given()
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
        return authToken;
    }

    public static Response deposit(String authHeader, int accountId, float amount) {
        Map<String, Object> depositBody = new HashMap<>();
        depositBody.put("id", accountId);
        depositBody.put("balance", amount);

        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .body(depositBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    public static void depositMultipleTimes(String authHeader, int accountId, float amount, int times) {
        for (int i = 0; i < times; i++) {
            deposit(authHeader, accountId, amount);
        }
    }

    public static Response transfer(String authHeader, int senderAccountId, int receiverAccountId, float amount, int statusCode) {
        Map<String, Object> depositBody = new HashMap<>();
        depositBody.put("senderAccountId", senderAccountId);
        depositBody.put("receiverAccountId", receiverAccountId);
        depositBody.put("amount", amount);

        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .body(depositBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(statusCode)
                .extract()
                .response();
    }

    public static float getBalance(String authHeader, int accountId) {
        float balance =
                given()
                        .header("Authorization", authHeader)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .get("/api/v1/customer/accounts")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .body("id", hasItem(accountId))
                        .body("transactions", not(empty()))
                        .extract()
                        .path("find { it.id == " + accountId + " }.balance");
        return balance;
    }

    public static void deleteAllAccounts(Map<String, List<Integer>> countsIds) {
        for (Map.Entry<String, List<Integer>> entry : countsIds.entrySet()) {
            String authHeader = entry.getKey();
            List<Integer> accountIds = entry.getValue();

            for (int accountId : accountIds) {
                try {
                    deleteUsersAccount(authHeader, accountId);
                } catch (Exception e) {
                    System.err.println("Failed to delete account " + accountId + ": " + e.getMessage());
                }
            }
        }
    }

    public static Response getUsers(String authHeader, int statusCode) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(statusCode)
                .extract()
                .response();
    }
}
