package nBankTests.iteration2_senior_level;

import generatos.RandomData;
import models.accounts.OperationType;
import models.accounts.Transaction;
import models.accounts.DepositMoneyRequest;
import models.accounts.DepositMoneyResponse;
import models.comparison.ModelAssertions;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.AccountData;
import utils.UserData;

import java.util.stream.Stream;

import static generatos.RandomData.getDepositAmount;
import static org.assertj.core.api.Assertions.within;

public class DepositTest extends BaseTest {
    private static AccountData account_1;
    private static AccountData account_2;
    private static UserData user_1;
    private static UserData user_2;
    private static final String ERROR_VALUE = "Unauthorized access to account";

    @BeforeAll
    public static void createTestData() {
        user_1 = AdminSteps.createUser();
        account_1 = UserSteps.createAccount(user_1);

        user_2 = AdminSteps.createUser();
        account_2 = UserSteps.createAccount(user_2);
    }

    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        // 1. Первый депозит — случайная сумма
        Double firstTransactionAmount = getDepositAmount();
        Double currentBalance = firstTransactionAmount;

        DepositMoneyResponse userDepositResponse = UserSteps.deposit(user_1, account_1, firstTransactionAmount);
        assertDepositResponse(userDepositResponse, account_1, currentBalance, 1);
        Transaction firstTransaction = userDepositResponse.getTransactions().get(0);

        checkTransaction(firstTransaction, account_1, firstTransactionAmount);

        // 2. Второй депозит — верхняя граница (5000.00)
        double secondTransactionAmount = 5000.0;
        currentBalance = UserSteps.getBalance(user_1, account_1);
        userDepositResponse = UserSteps.deposit(user_1, account_1, secondTransactionAmount);
        currentBalance += secondTransactionAmount;
        assertDepositResponse(userDepositResponse, account_1, currentBalance, 2);

        // 3. Третий депозит — нижняя граница (0.01)
        double thirdTransactionAmount = 0.01;
        currentBalance = UserSteps.getBalance(user_1, account_1);
        userDepositResponse = UserSteps.deposit(user_1, account_1, thirdTransactionAmount);
        currentBalance += thirdTransactionAmount;
        assertDepositResponse(userDepositResponse, account_1, currentBalance, 3);
        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(currentBalance, within(0.005));
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(-1.0, "Deposit amount must be at least 0.01", account_1.id(), user_1),
                Arguments.of(0.0, "Deposit amount must be at least 0.01", account_1.id(), user_1),
                Arguments.of(5001.0, "Deposit amount cannot exceed 5000", account_1.id(), user_1)
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotDepositWithInvalidAmount(Double amount, String errorValue, int accountId, UserData user) {
        double currentBalance = UserSteps.getBalance(user, account_1);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturns400WithoutKeyValue(errorValue))
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user, account_1)).isEqualTo(currentBalance, within(0.005));
    }

    @Test
    public void userCanNotDepositToNotOwnedAccount() {
        Double amount = RandomData.getDepositAmount();
        Double balanceBeforeDeposit = UserSteps.getBalance(user_1, account_1);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(account_2.id())
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user_1.username(), user_1.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(ERROR_VALUE))
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(balanceBeforeDeposit, within(0.005));
    }


    @Test
    public void userCanNotDepositWithExpiredAuthToken() {
        double balanceBeforeDeposit = UserSteps.getBalance(user_1, account_1);
        double amount = RandomData.getDepositAmount();
        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(account_1.id())
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUserInvalidToken(user_1.username(), user_1.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(balanceBeforeDeposit, within(0.005));
    }

    @Test
    public void userCannotDepositWithoutToken() {
        double balanceBeforeDeposit = UserSteps.getBalance(user_1, account_1);
        double amount = RandomData.getDepositAmount();
        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(account_1.id())
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsUnauthorized())
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(balanceBeforeDeposit, within(0.005));
    }

    public static Stream<Arguments> invalidDepositRequests() {
        return Stream.of(
                Arguments.of(null, RandomData.getDepositAmount()),
                Arguments.of(account_1.id(), null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositRequests")
    public void userCannotDepositWithInvalidFields(Integer id, Double balance) {
        double balanceBeforeDeposit = UserSteps.getBalance(user_1, account_1);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(id)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user_1.username(), user_1.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsInternalServerError())
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(balanceBeforeDeposit, within(0.005));
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAccount(user_1, account_1.id());
        UserSteps.deleteAccount(user_2, account_2.id());
        AdminSteps.deleteUser(user_1);
        AdminSteps.deleteUser(user_2);
    }

    private void checkTransaction(Transaction transaction, AccountData account, Double expectedAmount) {
        softly.assertThat(transaction.getId()).isNotNull();
        softly.assertThat(transaction.getAmount()).isEqualTo(expectedAmount);
        softly.assertThat(transaction.getType()).isEqualTo(OperationType.DEPOSIT.toString());
        softly.assertThat(transaction.getTimestamp()).isNotNull();
        softly.assertThat(transaction.getRelatedAccountId()).isEqualTo(account.id());
    }

    private void assertDepositResponse(
            DepositMoneyResponse response,
            AccountData expectedAccount,
            Double expectedBalance,
            int expectedTransactionCount
    ) {
        ModelAssertions.assertThatModels(expectedAccount, response).match();

        softly.assertThat(response.getBalance()).isEqualTo(expectedBalance, within(0.005));
        softly.assertThat(response.getTransactions()).hasSize(expectedTransactionCount);
    }
}

