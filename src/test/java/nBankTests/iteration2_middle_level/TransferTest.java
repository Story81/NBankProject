package nBankTests.iteration2_middle_level;

import generatos.RandomData;
import models.accounts.TransferMoneyRequest;
import models.accounts.TransferMoneyResponse;
import nBankTests.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.requesters.TransferMoneyRequestSender;
import specs.RequestSpecs;
import specs.ResponceSpecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static nBankTests.CommonSteps.createUser;
import static nBankTests.CommonSteps.createUsersAccount;
import static nBankTests.CommonSteps.deleteAllAccounts;
import static nBankTests.CommonSteps.deleteAllUsers;
import static nBankTests.CommonSteps.depositMultipleTimes;
import static nBankTests.CommonSteps.getAuthToken;
import static nBankTests.CommonSteps.getBalance;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferTest extends BaseTest {
    private static final String username = RandomData.getUserName();
    private static String password = RandomData.getPassword();
    private static final String userReceiverName = RandomData.getUserName();
    private static String userReceiverAuthHeader;
    private static int accountId;
    private static String userAuthHeader;
    private static Map<String, List<Integer>> accountsIds = new HashMap<>();
    private static List<String> createdUserIds = new ArrayList<>();
    private static final Float MAX_DEPOSIT_AMOUNT = 5000.0f;
    public static final String TRANSFER_SUCCESSFUL = "Transfer successful";
    public static final String INVALID_TRANSFER = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String UNAUTHORIZED_ACCESS_TO_ACCOUNT = "Unauthorized access to account";

    @BeforeAll
    public static void createTestData() {
        String userId = createUser(username, password);
        createdUserIds.add(userId);
        accountId = createUsersAccount(username, password);
        userAuthHeader = getAuthToken(username, password);
        addAccountToMap(userAuthHeader, accountId);
    }

    public static Stream<Arguments> transferValidData() {
        int receiverAccountId = createUsersAccount(username, password);
        addAccountToMap(userAuthHeader, receiverAccountId);
        return Stream.of(
                Arguments.of(0.01, receiverAccountId),
                Arguments.of(0.02, receiverAccountId),
                Arguments.of(5000.0, receiverAccountId),
                Arguments.of(9999.99, receiverAccountId),
                Arguments.of(10000.0, receiverAccountId)
        );
    }

    @ParameterizedTest
    @MethodSource("transferValidData")
    public void userCanTransferToOwnAccountTest(Double amount, int receiverAccountId) {
        depositMultipleTimes(userAuthHeader, accountId, MAX_DEPOSIT_AMOUNT, 4);
        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float balanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturnsOK())
                .post(transferMoneyRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(accountId);
        softly.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferMoneyResponse.getAmount().floatValue()).isEqualTo(amount.floatValue());
        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo(TRANSFER_SUCCESSFUL);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount)
                .isCloseTo(balanceAccountBeforeTransfer - amount.floatValue(), within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount)
                .isCloseTo(balanceReceiverAccountBeforeTransfer + amount.floatValue(), within(0.01f));
    }

    @Test
    public void userCanTransferToAccountOfOtherUserTest() {
        String userId = createUser(userReceiverName, password);
        createdUserIds.add(userId);
        userReceiverAuthHeader = getAuthToken(userReceiverName, password);
        int accountId = createAndDepositAccount(username, password, userAuthHeader, 5000.0f, 1);
        int receiverAccountId = createAndDepositAccount(userReceiverName, password, userReceiverAuthHeader, null, null);
        float currentBalanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        Double amount = RandomData.getDepositAmount().doubleValue();

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturnsOK())
                .post(transferMoneyRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(accountId);
        softly.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferMoneyResponse.getAmount().floatValue()).isEqualTo(amount.floatValue());
        softly.assertThat(transferMoneyResponse.getMessage()).isEqualTo(TRANSFER_SUCCESSFUL);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount)
                .isCloseTo(currentBalanceAccountBeforeTransfer - amount.floatValue(), within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userReceiverAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount)
                .isCloseTo(amount.floatValue(), within(0.01f));
    }

    @Test
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        int accountId = createAndDepositAccount(username, password, userAuthHeader, 1.0f, 1);
        int receiverAccountId = createAndDepositAccount(username, password, userAuthHeader, null, null);
        Double amount = RandomData.getDepositAmount().doubleValue();

        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float balanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER))
                .post(transferMoneyRequest);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01f));
    }

    @Test
    public void userCanNotTransferOnNonExistentAccount() {
        int accountId = createAndDepositAccount(username, password, userAuthHeader, MAX_DEPOSIT_AMOUNT, 1);
        int receiverAccountId = 654511854;
        Double amount = RandomData.getDepositAmount().doubleValue();
        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturns400WithoutKeyValue(INVALID_TRANSFER))
                .post(transferMoneyRequest);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));
    }

    @Test
    public void userCanNotTransferFromNonExistentAccount() {
        int accountId = 654511854;
        int receiverAccountId = createAndDepositAccount(username, password, userAuthHeader, null, null);
        Double amount = RandomData.getDepositAmount().doubleValue();

        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturnsForbidden(UNAUTHORIZED_ACCESS_TO_ACCOUNT))
                .post(transferMoneyRequest);

        float balanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(balanceReceiverAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));
    }

    @Test
    public void userCanNotTransferWithExpiredAuthToken() {
        int accountId = createAndDepositAccount(username, password, userAuthHeader, MAX_DEPOSIT_AMOUNT, 1);
        int receiverAccountId = createAndDepositAccount(username, password, userAuthHeader, null, null);
        Double amount = RandomData.getDepositAmount().doubleValue();
        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float balanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(RequestSpecs.authAsUserInvalidToken(username, password),
                ResponceSpecs.requestReturnsUnauthorized())
                .post(transferMoneyRequest);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01f));
    }

    @Test
    public void userCannotTransferWithoutToken() {
        int accountId = createAndDepositAccount(username, password, userAuthHeader, MAX_DEPOSIT_AMOUNT, 1);
        int receiverAccountId = createAndDepositAccount(username, password, userAuthHeader, null, null);
        Double amount = RandomData.getDepositAmount().doubleValue();
        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float balanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(RequestSpecs.unauthSpec(),
                ResponceSpecs.requestReturnsUnauthorized())
                .post(transferMoneyRequest);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01f));
    }

    public static Stream<Arguments> transferInvalidData() {
        int receiverAccountId = createAndDepositAccount(username, password, userAuthHeader, null, null);
        return Stream.of(
                Arguments.of(0.0f, receiverAccountId, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.1f, receiverAccountId, "Transfer amount cannot exceed 10000"),
                Arguments.of(-5001.0f, receiverAccountId, "Transfer amount must be at least 0.01")
        );
    }

    @ParameterizedTest
    @MethodSource("transferInvalidData")
    public void userCanNotTransferWithIncorrectAmountTest(Float amount, int receiverAccountId, String errorMessage) {
        float balanceAccountBeforeTransfer = getBalance(userAuthHeader, accountId);
        float balanceReceiverAccountBeforeTransfer = getBalance(userAuthHeader, receiverAccountId);

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount.doubleValue())
                .build();

        new TransferMoneyRequestSender(RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturns400WithoutKeyValue(errorMessage))
                .post(transferMoneyRequest);

        float currentBalanceAccount = getBalance(userAuthHeader, accountId);
        softly.assertThat(currentBalanceAccount).isCloseTo(balanceAccountBeforeTransfer, within(0.01f));

        float currentBalanceReceiverAccount = getBalance(userAuthHeader, receiverAccountId);
        softly.assertThat(currentBalanceReceiverAccount).isCloseTo(balanceReceiverAccountBeforeTransfer, within(0.01f));
    }

    public static Stream<Arguments> invalidTransferRequests() {
        Double amount = RandomData.getDepositAmount().doubleValue();
        return Stream.of(
                Arguments.of(null, accountId, amount, "senderAccountId is null"),
                Arguments.of(accountId, null, amount, "receiverAccountId is null"),
                Arguments.of(accountId, accountId, null, "amount is null")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferRequests")
    public void userCannotTransferWithNullFields(Integer senderId, Integer receiverId, Double amount, String description) {
        float balanceBefore = getBalance(userAuthHeader, accountId);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();

        new TransferMoneyRequestSender(
                RequestSpecs.authAsUser(userAuthHeader),
                ResponceSpecs.requestReturnsInternalServerError())
                .post(request);

        softly.assertThat(getBalance(userAuthHeader, accountId))
                .as("Balance changed when %s", description)
                .isEqualTo(balanceBefore);
    }
    //вспомогательные методы
    public static void addAccountToMap(String authHeader, int accountId) {
        accountsIds.computeIfAbsent(authHeader, k -> new ArrayList<>()).add(accountId);
    }

    private static int createAndDepositAccount(String name, String password, String authHeader, Float amount, Integer count) {
        int accountId = createUsersAccount(name, password);
        addAccountToMap(authHeader, accountId);
        if (amount != null && count != null) {
            depositMultipleTimes(authHeader, accountId, amount, count);
        }
        return accountId;
    }

    @AfterAll
    public static void deleteTestData() {
        deleteAllAccounts(accountsIds);
        deleteAllUsers(createdUserIds);
    }
}
