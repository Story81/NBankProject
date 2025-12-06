package nBankTests.api.iteration2_senior_level;

import api.generatos.RandomData;
import api.models.accounts.TransferMoneyRequest;
import api.models.accounts.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.Account;
import common.annotations.UserSession;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static api.models.accounts.TransferMoneyResponse.TRANSFER_SUCCESSFUL;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferPositiveTest extends BaseTest {
    private static final Double MAX_DEPOSIT_AMOUNT = 5000.00;


    @ParameterizedTest
    @CsvSource({"0.01", "0.02", "5000.0", "9999.99", "10000.0"})
    @UserSession
    @Account(value = 2)
    public void userCanTransferToOwnAccountTest(Double amount) {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
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
    @UserSession(2)
    @Account(user = 2, value = 1)
    public void userCanTransferToAccountOfOtherUserTest() {
        SessionStorage.printState();
        UserData user = SessionStorage.getUser(1);
        UserData receiverUser = SessionStorage.getUser(2);
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(receiverUser, 1);
        UserSteps.depositMultipleTimes(user, account, MAX_DEPOSIT_AMOUNT, 1);

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
}
