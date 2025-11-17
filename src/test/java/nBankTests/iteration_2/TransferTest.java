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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.createUsersAccount;
import static nBankTests.CommonSteps.deleteAllAccounts;
import static nBankTests.CommonSteps.deleteUser;
import static nBankTests.CommonSteps.deposit;
import static nBankTests.CommonSteps.depositMultipleTimes;
import static nBankTests.CommonSteps.getAuthTokenTest;
import static nBankTests.CommonSteps.getBalance;
import static nBankTests.CommonSteps.transfer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferTest extends BaseTest {
    private static final String username = "Kate_" + UUID.randomUUID().toString().substring(0, 8);
    private static String password = "Kate2000#";
    private static String role = "USER";
    private static String userId;
    private static String userReceiverId;
    private static final String userReceiverName = "Mark_" + UUID.randomUUID().toString().substring(0, 8);
    private static String userReceiverPassword = "Mark_2000#";
    private static String userReceiverAuthHeader;
    private static int accountId;
    private static String userAuthHeader;
    private static String expiredAuthToken = "00F0ZV00NnEzMjVhNDpLYXRlM=";
    private static Map<String, List<Integer>> accountsIds = new HashMap<>();

    @BeforeAll
    public static void createTestData() {
        userId = createUser(username, password, role);
        userAuthHeader = getAuthTokenTest(username, password);
        accountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, accountId);
        deposit(userAuthHeader, accountId, 5000.0f);
        userReceiverId = createUser(userReceiverName, userReceiverPassword, role);
        userReceiverAuthHeader = getAuthTokenTest(userReceiverName, userReceiverPassword);
    }

    public static Stream<Arguments> transferValidData() {
        int receiverAccountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, receiverAccountId);
        return Stream.of(
                Arguments.of(0.01f, receiverAccountId),
                Arguments.of(0.02f, receiverAccountId),
                Arguments.of(5000.0f, receiverAccountId),
                Arguments.of(9999.99f, receiverAccountId),
                Arguments.of(10000.0f, receiverAccountId)
        );
    }

    @ParameterizedTest
    @MethodSource("transferValidData")
    public void userCanTransferToOwnAccountTest(float amount, int receiverAccountId) {
        depositMultipleTimes(userAuthHeader, accountId, 5000.0f, 4);
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float currentBalanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_OK)
                .then()
                .body("senderAccountId", equalTo(accountId))
                .body("receiverAccountId", equalTo(receiverAccountId))
                .body("amount", equalTo(amount))
                .body("message", equalTo("Transfer successful"));

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer - amount));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        assertThat(currentBalanceReceiverAccount, equalTo(currentBalanceReceiverAccountBeforeTransfer + amount));
    }

    @Test
    public void userCanTransferToAccountOfOtherUserTest() {
        int receiverAccountId = createUsersAccount(userReceiverAuthHeader);
        addAccountToMap(userReceiverAuthHeader, receiverAccountId);
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float amount = 100.0f;
        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_OK)
                .then()
                .body("senderAccountId", equalTo(accountId))
                .body("receiverAccountId", equalTo(receiverAccountId))
                .body("amount", equalTo(amount))
                .body("message", equalTo("Transfer successful"));

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer - amount));

        float currentBalanceReceiverAccount = getBalance(userReceiverAuthHeader, receiverAccountId);
        assertThat(currentBalanceReceiverAccount, equalTo(amount));
    }

    @Test
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        int accountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, accountId);
        int receiverAccountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, receiverAccountId);
        float amount = 10000.0f;
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float currentBalanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_BAD_REQUEST)
                .then()
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        assertThat(currentBalanceReceiverAccount, equalTo(currentBalanceReceiverAccountBeforeTransfer));
    }

    @Test
    public void userCanNotTransferOnNonExistentAccount() {
        int accountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, accountId);
        int receiverAccountId = 654511854;
        float amount = 10.0f;
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);

        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_BAD_REQUEST)
                .then()
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));
    }

    @Test
    public void userCanNotTransferFromNonExistentAccount() {
        int accountId = 564515;
        int receiverAccountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, receiverAccountId);
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);
        float amount = 10.0f;

        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_FORBIDDEN)
                .then()
                .body(equalTo("Unauthorized access to account"));

        float currentBalanceAccount = getBalance(userAuthHeader, receiverAccountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));
    }

    @Test
    public void userCanNotTransferWithExpiredAuthToken() {
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        Map < String,
                Object > requestBody = new HashMap <  > ();
        requestBody.put("id", accountId);
        requestBody.put("balance", 10.0f);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", expiredAuthToken)
                .body(requestBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));
    }

    @Test
    public void userCannotTransferWithoutToken() {
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float amount = 10.0f;
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transfer(null, accountId, accountId, amount, HttpStatus.SC_BAD_REQUEST));
        assertEquals("Header value cannot be null", exception.getMessage());
        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
    }

        public static Stream<Arguments> transferInvalidData() {
        int receiverAccountId = createUsersAccount(userAuthHeader);
        addAccountToMap(userAuthHeader, receiverAccountId);
        return Stream.of(
                Arguments.of(0.0f, receiverAccountId, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.1f, receiverAccountId, "Transfer amount cannot exceed 10000"),
                Arguments.of(-5001.0f, receiverAccountId, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCanNotTransferWithIncorrectAmountTest(Float amount, int receiverAccountId, String errorMessage) {
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float currentBalanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        transfer(userAuthHeader, accountId, receiverAccountId, amount, HttpStatus.SC_BAD_REQUEST)
                .then()
                .body(equalTo(errorMessage));

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        assertThat(currentBalanceReceiverAccount, equalTo(currentBalanceReceiverAccountBeforeTransfer));
    }

    public static Stream<Arguments> invalidRequestBodyData() {
        return Stream.of(

                Arguments.of("""
                        {"senderAccountId":%s}
                        """.formatted(accountId)), // нет receiverAccountId и amount

                Arguments.of("""
                        {"receiverAccountId":%s}
                        """.formatted(accountId)), // нет senderAccountId и amount

                Arguments.of("""
                        {"amount":100.0}
                        """), // нет senderAccountId и receiverAccountId

                Arguments.of("""
                        {}
                        """),

                Arguments.of("""
                        {"senderAccountId":%s, "receiverAccountId":%s}
                        """.formatted(accountId, accountId)), // нет amount

                Arguments.of("""
                        {"senderAccountId":%s, "amount":100.0}
                        """.formatted(accountId)), // нет receiverAccountId

                Arguments.of("""
                        {"receiverAccountId":%s, "amount":100.0}
                        """.formatted(accountId)), // нет senderAccountId

                // Неправильные типы данных
                Arguments.of("""
                        {"senderAccountId":"string", "receiverAccountId":%s, "amount":100.0}
                        """.formatted(accountId)), // senderAccountId как строка

                Arguments.of("""
                        {"senderAccountId":%s, "receiverAccountId":"string", "amount":100.0}
                        """.formatted(accountId)), // receiverAccountId как строка

                Arguments.of("""
                        {"senderAccountId":%s, "receiverAccountId":%s, "amount":"string"}
                        """.formatted(accountId, accountId)), // amount как строка

                Arguments.of("""
                        {"senderAccountId":null, "receiverAccountId":%s, "amount":100.0}
                        """.formatted(accountId)),

                Arguments.of("""
                        {"senderAccountId":%s, "receiverAccountId":null, "amount":100.0}
                        """.formatted(accountId)),

                Arguments.of("""
                        {"senderAccountId":%s, "receiverAccountId":%s, "amount":null}
                        """.formatted(accountId, accountId))
        );
    }

    @MethodSource("invalidRequestBodyData")
    @ParameterizedTest
    public void userCanNotDepositWithInvalidRequestBody(String requestBody) {
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(requestBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        assertThat(currentBalanceAccount, equalTo(currentBalanceAccountBeforeTransfer));
    }

    public static void addAccountToMap(String authHeader, int accountId) {
        accountsIds.computeIfAbsent(authHeader, k -> new ArrayList<>()).add(accountId);
    }

    @AfterAll
    public static void deleteTestDate() {
        deleteAllAccounts(accountsIds);
        deleteUser(ADMIN_AUTH, userId);
        deleteUser(ADMIN_AUTH, userReceiverId);
    }
}
