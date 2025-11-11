package nBankTests.iteration_2;

import io.restassured.http.ContentType;
import nBankTests.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.createUsersAccount;
import static nBankTests.CommonSteps.deleteUser;
import static nBankTests.CommonSteps.deleteUsersAccount;
import static nBankTests.CommonSteps.deposit;
import static nBankTests.CommonSteps.getAuthTokenTest;

import static nBankTests.CommonSteps.getBalance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DepositTest extends BaseTest {
    private static final String username = "Kate_" + UUID.randomUUID().toString().substring(0, 8);
    private static String password = "Kate2000#";
    private static String role = "USER";
    private static String userId;
    private static int accountId;
    private static String userAuthHeader;
    private static String expiredAuthToken = "00F0ZV00NnEzMjVhNDpLYXRlM=";

    @BeforeAll
    public static void createTestData() {
        userId = createUser(username, password, role);
        userAuthHeader = getAuthTokenTest(username, password);
        accountId = createUsersAccount(userAuthHeader);
    }

    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        float firstTransactionAmount = 1000.00f;
        float currentBalance = firstTransactionAmount;
        deposit(userAuthHeader, accountId, firstTransactionAmount)
                .then()
                .body("id", equalTo(accountId))
                .body("accountNumber", equalTo("ACC" + accountId))
                .body("balance", equalTo(currentBalance))
                .body("transactions", hasSize(1))
                .body("transactions[0].id", notNullValue())
                .body("transactions[0].amount", equalTo(firstTransactionAmount))
                .body("transactions[0].type", equalTo("DEPOSIT"))
                .body("transactions[0].relatedAccountId", equalTo(accountId))
                .body("transactions[0].timestamp", notNullValue());


        float secondTransactionAmount = 5000.0f;
        float thirdTransactionAmount = 0.01f;

        currentBalance = getBalance(userAuthHeader, accountId) + secondTransactionAmount;
        deposit(userAuthHeader, accountId, secondTransactionAmount)
                .then()
                .body("id", equalTo(accountId))
                .body("accountNumber", equalTo("ACC" + accountId))
                .body("balance", equalTo(currentBalance))
                .body("transactions", hasSize(2));

        currentBalance += thirdTransactionAmount;
        deposit(userAuthHeader, accountId, thirdTransactionAmount)
                .then()
                .body("id", equalTo(accountId))
                .body("accountNumber", equalTo("ACC" + accountId))
                .body("balance", equalTo(currentBalance))
                .body("transactions", hasSize(3));
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(-1.0f, "Deposit amount must be at least 0.01", accountId, userAuthHeader),
                Arguments.of(0.0f, "Deposit amount must be at least 0.01", accountId, userAuthHeader),
                Arguments.of(5001.0f, "Deposit amount cannot exceed 5000", accountId, userAuthHeader)
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotDepositWithInvalidAmount(Float amount, String errorValue, int accountId, String authHeader) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", accountId);
        requestBody.put("balance", amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo(errorValue));
    }

    @Test
    public void userCanNotDepositToNotOwnedAccount() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", 346);
        requestBody.put("balance", 4000.0f);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(equalTo("Unauthorized access to account"));
    }

    @Test
    public void userCanNotDepositWithExpiredAuthToken() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", accountId);
        requestBody.put("balance", 10.0f);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", expiredAuthToken)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void userCannotDepositWithoutToken() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deposit(null, accountId, 10.0f));
        assertEquals("Header value cannot be null", exception.getMessage());
    }

    public static Stream<Arguments> invalidRequestBodyData() {
        return Stream.of(
                Arguments.of("""
                         {"id":%s}
                        """.formatted(accountId)),
                Arguments.of("""
                        {"balance":100.0}
                        """),
                Arguments.of("""
                        {}
                        """),
                Arguments.of("""
                        {"id":"string",
                        "balance":100.0}
                        """),
                Arguments.of("""
                        {"id":%s,
                        "balance":"value"}
                        """.formatted(accountId)),
                Arguments.of("""
                        {"id":null,
                        "balance":"100.0"}
                        """),
                Arguments.of("""
                         {"id":%s,
                         "balance":null}
                        """.formatted(accountId))
        );
    }

    @MethodSource("invalidRequestBodyData")
    @ParameterizedTest
    public void userCanNotDepositWithInvalidRequestBody(String requestBody) {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteUsersAccount(userAuthHeader, accountId);
        deleteUser(ADMIN_AUTH, userId);
    }
}
