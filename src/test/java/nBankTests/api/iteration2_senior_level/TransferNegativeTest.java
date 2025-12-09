package nBankTests.api.iteration2_senior_level;

import api.generatos.RandomData;
import api.models.accounts.TransferMoneyRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.ApiUserSession;
import common.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.Account;
import nBankTests.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static api.generatos.RandomData.generateRandomAccountId;
import static api.generatos.RandomData.getDepositAmount;
import static api.models.BankAlert.INVALID_TRANSFER_ERROR_MESSAGE;
import static api.models.BankAlert.UNAUTHORIZED_ACCESS_ERROR;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferNegativeTest extends BaseTest {

    @Test
    @ApiUserSession
    @Account(value = 2)
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
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
                ResponseSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER_ERROR_MESSAGE.getMessage()))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    @Test
    @ApiUserSession
    @Account
    public void userCanNotTransferOnNonExistentAccount() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);

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
                ResponseSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER_ERROR_MESSAGE.getMessage()))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
    }

    @Test
    @ApiUserSession
    @Account(value = 2)
    public void userCanNotTransferFromNonExistentAccount() {
        UserData user = SessionStorage.getUser();
        AccountData receiverAccount = SessionStorage.getAccount(user, 1);
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
                ResponseSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_ERROR.getMessage()))
                .post(transferMoneyRequest);

        double balanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(balanceReceiverAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));
    }

    @Test
    @ApiUserSession
    @Account(value = 2)
    public void userCanNotTransferWithExpiredAuthToken() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
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
                ResponseSpecs.requestReturnsUnauthorized())
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    @Test
    @ApiUserSession
    @Account(value = 2)
    public void userCannotTransferWithoutToken() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
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
                ResponseSpecs.requestReturnsUnauthorized())
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
    @ApiUserSession
    @Account(value = 2)
    public void userCanNotTransferWithIncorrectAmountTest(Double amount, String errorMessage) {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
        double balanceAccountBeforeTransfer = UserSteps.getBalance(user, account);
        double balanceReceiverAccountBeforeTransfer = UserSteps.getBalance(user, receiverAccount);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account.id())
                .receiverAccountId(receiverAccount.id())
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(user.username(), user.password()),
                Endpoint.ACCOUNT_TRANSFER,
                ResponseSpecs.requestReturns400WithoutKeyValue(errorMessage))
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }

    public static Stream<Arguments> invalidTransferRequests() {
        Integer randomId = RandomData.generateRandomAccountId();
        return Stream.of(
                Arguments.of(null, randomId, getDepositAmount()),
                Arguments.of(randomId, null, getDepositAmount()),
                Arguments.of(randomId, randomId, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferRequests")
    @ApiUserSession
    @Account(value = 2)
    public void userCannotTransferWithNullFields(Integer senderAccountId, Integer receiverAccountId,
                                                 Double amount) {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getAccount(user, 1);
        AccountData receiverAccount = SessionStorage.getAccount(user, 2);
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
                ResponseSpecs.requestReturnsInternalServerError())
                .post(transferMoneyRequest);

        double currentBalanceAccount = UserSteps.getBalance(user, account);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01));

        double currentBalanceReceiverAccount = UserSteps.getBalance(user, receiverAccount);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01));
    }
}
