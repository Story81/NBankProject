package nBankTests.iteration2_middle_level;

import generatos.RandomData;
import models.accounts.OperationType;
import models.accounts.Transaction;
import models.accounts.DepositMoneyRequest;
import models.accounts.DepositMoneyResponse;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.requesters.DepositMoneyRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.stream.Stream;

import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.createUsersAccount;
import static nBankTests.CommonSteps.deleteUser;
import static nBankTests.CommonSteps.deleteUsersAccount;
import static nBankTests.CommonSteps.getAuthToken;
import static nBankTests.CommonSteps.getBalance;

public class DepositTest extends BaseTest {
    private static final String username = RandomData.getUserName();
    private static final String username_2 = RandomData.getUserName();
    private static String password = RandomData.getPassword();
    private static String userId;
    private static String userId_2;
    private static int accountId;
    private static int accountId_2;
    private static String userAuthHeader;
    private static String userAuthHeader_2;
    public static final String UNAUTHORIZED_ACCESS_TO_ACCOUNT = "Unauthorized access to account";

    @BeforeAll
    public static void createTestData() {
        userId = createUser(username, password);
        userAuthHeader = getAuthToken(username, password);
        accountId = createUsersAccount(username, password);

        userId_2 = createUser(username_2, password);
        userAuthHeader_2 = getAuthToken(username_2, password);
        accountId_2 = createUsersAccount(username_2, password);
    }

    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        float firstTransactionAmount = RandomData.getDepositAmount().floatValue();
        float currentBalance = firstTransactionAmount;

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(firstTransactionAmount)
                .build();

        DepositMoneyResponse userDepositResponse = new DepositMoneyRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsOK())
                .post(userDepositRequest)
                .extract()
                .as(DepositMoneyResponse.class);

        softly.assertThat(userDepositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(userDepositResponse.getAccountNumber()).isNotEmpty();
        softly.assertThat(userDepositResponse.getAccountNumber()).isEqualTo("ACC" + accountId);
        softly.assertThat(userDepositResponse.getBalance()).isEqualTo(currentBalance);
        softly.assertThat(userDepositResponse.getTransactions().size()).isEqualTo(1);

        Transaction firstTransaction = userDepositResponse.getTransactions().get(0);
        softly.assertThat(firstTransaction.getId()).isNotNull();
        softly.assertThat(firstTransaction.getAmount()).isEqualTo(firstTransactionAmount);
        softly.assertThat(firstTransaction.getType()).isEqualTo(OperationType.DEPOSIT.toString());
        softly.assertThat(firstTransaction.getRelatedAccountId()).isEqualTo(accountId);
        softly.assertThat(firstTransaction.getTimestamp()).isNotNull();


        float secondTransactionAmount = 5000.0f; // верхняя граница
        userDepositRequest.setBalance(secondTransactionAmount);

        currentBalance = getBalance(userAuthHeader, accountId) + secondTransactionAmount;
        userDepositResponse = new DepositMoneyRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsOK())
                .post(userDepositRequest)
                .extract()
                .as(DepositMoneyResponse.class);

        softly.assertThat(userDepositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(userDepositResponse.getAccountNumber()).isEqualTo("ACC" + accountId);
        softly.assertThat(userDepositResponse.getBalance()).isEqualTo(currentBalance);
        softly.assertThat(userDepositResponse.getTransactions().size()).isEqualTo(2);


        float thirdTransactionAmount = 0.01f; //нижняя граница
        userDepositRequest.setBalance(thirdTransactionAmount);
        currentBalance = getBalance(userAuthHeader, accountId);

        userDepositResponse = new DepositMoneyRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsOK())
                .post(userDepositRequest)
                .extract()
                .as(DepositMoneyResponse.class);

        currentBalance += thirdTransactionAmount;
        softly.assertThat(userDepositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(userDepositResponse.getAccountNumber()).isEqualTo("ACC" + accountId);
        softly.assertThat(userDepositResponse.getBalance()).isEqualTo(currentBalance);
        softly.assertThat(userDepositResponse.getTransactions().size()).isEqualTo(3);

        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(currentBalance);
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
    public void userCanNotDepositWithInvalidAmount(Float amount, String errorValue, int accountId) {
        float currentBalanceBeforeDeposit = getBalance(userAuthHeader, accountId);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new DepositMoneyRequestSender(RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturns400WithoutKeyValue(errorValue))
                .post(userDepositRequest);

        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(currentBalanceBeforeDeposit);
    }

    @Test
    public void userCanNotDepositToNotOwnedAccount() {
        float balance = RandomData.getDepositAmount().floatValue();
        float balanceBeforeDeposit = getBalance(userAuthHeader, accountId);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId_2)
                .balance(balance)
                .build();

        new DepositMoneyRequestSender(RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_TO_ACCOUNT))
                .post(userDepositRequest);

        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(balanceBeforeDeposit);
    }

    @Test
    public void userCanNotDepositWithExpiredAuthToken() {
        float balanceBeforeDeposit = getBalance(userAuthHeader, accountId);
        float balance = RandomData.getDepositAmount().floatValue();
        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new DepositMoneyRequestSender(
                RequestSpecs.authAsUserInvalidToken(username, password),
                ResponceSpecs.requestReturnsUnauthorized())
                .post(userDepositRequest);

        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(balanceBeforeDeposit);
    }

    @Test
    public void userCannotDepositWithoutToken() {
        float balanceBeforeDeposit = getBalance(userAuthHeader, accountId);
        float balance = RandomData.getDepositAmount().floatValue();
        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new DepositMoneyRequestSender(RequestSpecs.unauthSpec(), ResponceSpecs.requestReturnsUnauthorized())
                .post(userDepositRequest);
        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(balanceBeforeDeposit);
    }

    public static Stream<Arguments> invalidDepositRequests() {
        float balance = RandomData.getDepositAmount().floatValue();
        return Stream.of(
                Arguments.of(null, balance),
                Arguments.of(accountId, null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositRequests")
    public void userCannotDepositWithInvalidFields(Integer id, Float balance) {
        float balanceBefore = getBalance(userAuthHeader, accountId);

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(id)
                .balance(balance)
                .build();

        new DepositMoneyRequestSender(
                RequestSpecs.authAsUser(username, password),
                ResponceSpecs.requestReturnsInternalServerError())
                .post(request);
        softly.assertThat(getBalance(userAuthHeader, accountId)).isEqualTo(balanceBefore);
    }

    @AfterAll
    public static void deleteTestData() {
        deleteUsersAccount(userAuthHeader, accountId);
        deleteUsersAccount(userAuthHeader_2, accountId_2);
        deleteUser(ADMIN_AUTH, userId);
        deleteUser(ADMIN_AUTH, userId_2);
    }
}

