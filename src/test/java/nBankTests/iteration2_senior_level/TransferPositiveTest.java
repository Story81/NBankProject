package nBankTests.iteration2_senior_level;

import generatos.RandomData;
import models.accounts.TransferMoneyRequest;
import models.accounts.TransferMoneyResponse;
import models.comparison.ModelAssertions;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.AccountData;
import utils.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static models.accounts.TransferMoneyResponse.TRANSFER_SUCCESSFUL;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferPositiveTest extends BaseTest {
    private static final Double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static Map<UserData, List<Integer>> accountsIds = new HashMap<>();
    private static List<UserData> createdUsers = new ArrayList<>();


    public static Stream<Arguments> transferValidData() {
        UserData user = AdminSteps.createUser();
        createdUsers.add(user);

        AccountData receiverAccount = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, receiverAccount);

        return Stream.of(
                Arguments.of(0.01, receiverAccount, user),
                Arguments.of(0.02, receiverAccount, user),
                Arguments.of(5000.0, receiverAccount, user),
                Arguments.of(9999.99, receiverAccount, user),
                Arguments.of(10000.0, receiverAccount, user)
        );
    }

    @ParameterizedTest
    @MethodSource("transferValidData")
    public void userCanTransferToOwnAccountTest(Double amount, AccountData receiverAccount, UserData user) {
        AccountData account = UserSteps.createAccount(user);
        UserSteps.depositMultipleTimes(user, account, MAX_DEPOSIT_AMOUNT, 4);

        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOKWithMessage(TRANSFER_SUCCESSFUL))
                .post(transferMoneyRequest);

        assertSuccessfulTransfer(transferMoneyRequest, transferMoneyResponse, amount);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer - amount.floatValue(), within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer + amount, within(0.01));
    }

    @Test
    public void userCanTransferToAccountOfOtherUserTest() {
        UserData user = AdminSteps.createUser();
        createdUsers.add(user);
        AccountData account = UserSteps.createAccount(user);
        UserSteps.depositMultipleTimes(user, account, MAX_DEPOSIT_AMOUNT, 1);

        UserData receiverUser = AdminSteps.createUser();
        createdUsers.add(receiverUser);
        AccountData receiverAccount = UserSteps.createAccount(receiverUser);

        double currentBalanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        Double amount = RandomData.getDepositAmount();

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturnsOKWithMessage(TRANSFER_SUCCESSFUL))
                .post(transferMoneyRequest);

        assertSuccessfulTransfer(transferMoneyRequest, transferMoneyResponse, amount);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(currentBalanceAccountBeforeTransfer - amount, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(receiverUser, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(amount, within(0.01));
    }

    //вспомогательные методы

    private void assertSuccessfulTransfer(TransferMoneyRequest request, TransferMoneyResponse response,
                                          Double expectedAmount) {

        ModelAssertions.assertThatModels(request, response).match();
        softly.assertThat(response.getAmount().floatValue()).isEqualTo(expectedAmount.floatValue());
    }

    public static void addAccountToAccountsIdsMap(UserData user, AccountData account) {
        accountsIds.computeIfAbsent(user, k -> new ArrayList<>()).add(account.id());
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAllAccounts(accountsIds);
        AdminSteps.deleteAllUsers(createdUsers);
    }
}
