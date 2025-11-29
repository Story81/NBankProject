package nBankTests.iteration2_senior_level;

import generatos.RandomData;
import models.accounts.TransferMoneyRequest;
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
import specs.ResponceSpecs;
import utils.AccountData;
import utils.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static generatos.RandomData.generateRandomAccountId;
import static generatos.RandomData.getDepositAmount;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferNegativeTest extends BaseTest {
    private static UserData user;
    private static AccountData account;
    private static AccountData receiverAccount;
    private static final Double MIN_DEPOSIT_AMOUNT = 0.01;
    private static Map<UserData, List<Integer>> accountsIds = new HashMap<>();
    private static final String INVALID_TRANSFER_ERROR_MESSAGE = "Invalid transfer: insufficient funds or invalid accounts";
    private static final String UNAUTHORIZED_ACCESS_ERROR = "Unauthorized access to account";


    @BeforeAll
    public static void createTestData() {
        user = AdminSteps.createUser();
        account = UserSteps.createAndDepositAccount(user, MIN_DEPOSIT_AMOUNT, 1);
        receiverAccount = UserSteps.createAndDepositAccount(user, null, null);

        addAccountToAccountsIdsMap(user, account);
        addAccountToAccountsIdsMap(user, receiverAccount);
    }

    @Test
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        Double amount = getDepositAmount();

        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER_ERROR_MESSAGE))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    @Test
    public void userCanNotTransferOnNonExistentAccount() {
        Double amount = getDepositAmount();
        int receiverAccountId = generateRandomAccountId();

        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER_ERROR_MESSAGE))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
    }

    @Test
    public void userCanNotTransferFromNonExistentAccount() {
        int accountId = generateRandomAccountId();
        Double amount = getDepositAmount();

        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_ERROR))
                .post(transferMoneyRequest);

        double balanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(balanceReceiverAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
    }

    @Test
    public void userCanNotTransferWithExpiredAuthToken() {
        Double amount = getDepositAmount();

        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUserInvalidToken(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturnsUnauthorized())
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    @Test
    public void userCannotTransferWithoutToken() {
        Double amount = getDepositAmount();
        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturnsUnauthorized())
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(0.0, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.1, "Transfer amount cannot exceed 10000"),
                Arguments.of(-5001.0, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCanNotTransferWithIncorrectAmountTest(Double amount, String errorMessage) {
        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturns400WithoutKeyValue(errorMessage))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    public static Stream<Arguments> invalidTransferRequests() {
        return Stream.of(
                Arguments.of(user, null, receiverAccount.id(), getDepositAmount()),
                Arguments.of(user, account.id(), null, getDepositAmount()),
                Arguments.of(user, account.id(), receiverAccount.id(), null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferRequests")
    public void userCannotTransferWithNullFields(UserData user, Integer senderAccountId, Integer receiverAccountId,
                                                 Double amount) {
        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponceSpecs.requestReturnsInternalServerError())
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    //вспомогательные методы
    public static void addAccountToAccountsIdsMap(UserData user, AccountData account) {
        accountsIds.computeIfAbsent(user, k -> new ArrayList<>()).add(account.id());
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAllAccounts(accountsIds);
        AdminSteps.deleteUser(user);
    }
}
