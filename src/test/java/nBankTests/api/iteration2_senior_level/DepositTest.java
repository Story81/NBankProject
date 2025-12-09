package nBankTests.api.iteration2_senior_level;

import api.generatos.RandomData;
import api.models.accounts.DepositMoneyRequest;
import api.models.accounts.DepositMoneyResponse;
import api.models.accounts.OperationType;
import api.models.accounts.Transaction;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.Account;
import common.annotations.ApiUserSession;
import common.storage.SessionStorage;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static api.generatos.RandomData.getDepositAmount;
import static api.models.BankAlert.UNAUTHORIZED_ERROR_VALUE;
import static org.assertj.core.api.Assertions.within;

public class DepositTest extends BaseTest {

    @Test
    @ApiUserSession
    @Account
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getFirstAccount(user_1);

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
                Arguments.of(-1.0, "Deposit amount must be at least 0.01"),
                Arguments.of(0.0, "Deposit amount must be at least 0.01"),
                Arguments.of(5001.0, "Deposit amount cannot exceed 5000")
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    @ApiUserSession
    @Account
    public void userCanNotDepositWithInvalidAmount(Double amount, String errorValue) {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getFirstAccount(user_1);
        SessionStorage.printState();
        double currentBalance = UserSteps.getBalance(user_1, account_1);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(account_1.id())
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user_1.username(), user_1.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturns400WithoutKeyValue(errorValue))
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(currentBalance, within(0.005));
    }

    @Test
    @ApiUserSession
    @Account(value = 2)
    public void userCanNotDepositToNotOwnedAccount() {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getAccount(user_1, 1);
        Integer accountRandomId = RandomData.generateRandomAccountId();
        Double amount = RandomData.getDepositAmount();
        Double balanceBeforeDeposit = UserSteps.getBalance(user_1, account_1);

        DepositMoneyRequest userDepositRequest = DepositMoneyRequest.builder()
                .id(accountRandomId)
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user_1.username(), user_1.password()),
                Endpoint.ACCOUNT_DEPOSIT,
                ResponseSpecs.requestReturnsForbidden(UNAUTHORIZED_ERROR_VALUE.getMessage()))
                .post(userDepositRequest);

        softly.assertThat(UserSteps.getBalance(user_1, account_1)).isEqualTo(balanceBeforeDeposit, within(0.005));
    }

    @Test
    @ApiUserSession
    @Account
    public void userCanNotDepositWithExpiredAuthToken() {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getAccount(user_1, 1);
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
    @ApiUserSession
    @Account
    public void userCannotDepositWithoutToken() {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getAccount(user_1, 1);
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
                Arguments.of(RandomData.generateRandomAccountId(), null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositRequests")
    @ApiUserSession
    @Account
    public void userCannotDepositWithInvalidFields(Integer id, Double balance) {
        UserData user_1 = SessionStorage.getUser();
        AccountData account_1 = SessionStorage.getAccount(user_1, 1);
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

