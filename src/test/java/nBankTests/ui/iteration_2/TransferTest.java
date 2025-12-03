package nBankTests.ui.iteration_2;

import generatos.RandomData;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;
import utils.AccountData;
import utils.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static generatos.RandomData.generateRandomAccountId;
import static generatos.RandomData.getDepositAmount;
import static nBankTests.api.iteration2_senior_level.TransferPositiveTest.addAccountToAccountsIdsMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static ui.pages.BankAlert.ERROR_INVALID_TRANSFER;
import static ui.pages.BankAlert.ERROR_NO_USER_FOUND_WITH_THIS_ACCOUNT;
import static ui.pages.BankAlert.TRANSFER_SUCCESS;

public class TransferTest extends BaseUiTest {
    UserDashboard dashboard = new UserDashboard();
    private static UserData user;
    private static AccountData account_1;
    private static AccountData account_2;
    private final Double depositAmount = 5000.00;
    private static List<UserData> createdUsers = new ArrayList<>();
    private static Map<UserData, List<Integer>> accountsIds = new HashMap<>();

    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUsers.add(user);
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        addAccountToAccountsIdsMap(user, account_1);
        account_2 = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account_2);

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);
        String userName = RandomData.getUserName();

        //шаги
        authAsUser(user);

        TransferPage transferPage = dashboard.open()
                .clickTransferButton();           // юзер кликает на Make a Transfer

        transferPage.checkTransferHeader()                //Проверка элементов на странице
                .checkSelectAccountField()                 //Проверяем отображение поля
                .selectSenderAccount(account_1.accountNumber())      //Выбираем счет
                .inputRecipientName(userName)             // Вводим имя получателя
                .selectRecipientAccount(account_2.accountNumber())   // Выбираем номер счета получателя
                .inputTransferAmount(transferAmount)
                .clickConfirmDetailsCheckbox()             //Клик чекбокс проверки корректности деталей операции
                .clickTransferButton()
                .checkAlertMessageAndAccept(TRANSFER_SUCCESS.format(transferAmount, account_2.accountNumber()))
                .shouldBeOpened()
                .refreshPage();

        //проверка баланса счета отправителя
        Double accountBalanceAfter_1 = accountBalanceBeforeTransfer_1 - transferAmount;
        transferPage.checkSenderAccountBalance(account_1.accountNumber(), accountBalanceAfter_1);
        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceAfter_1, within(0.001));

        //проверка баланса счета получателя
        Double accountBalanceAfter_2 = accountBalanceBeforeTransfer_2 + transferAmount;
        transferPage.checkReceivedAccountBalance(account_2.accountNumber(), accountBalanceAfter_2);
        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isEqualTo(accountBalanceAfter_2, within(0.001));
    }

    @Test
    public void userCanDepositWithEmptyRecipientNameTest() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUsers.add(user);
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        addAccountToAccountsIdsMap(user, account_1);
        account_2 = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account_2);

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);

        //шаги
        authAsUser(user);

        TransferPage transferPage = dashboard.open()
                .clickTransferButton();           // юзер кликает на Make a Transfer

        transferPage.checkTransferHeader()                //Проверка элементов на странице
                .checkSelectAccountField()                 //Проверяем отображение поля
                .selectSenderAccount(account_1.accountNumber())      //Выбираем счет
                .selectRecipientAccount(account_2.accountNumber())   // Выбираем номер счета получателя
                .inputTransferAmount(transferAmount)
                .clickConfirmDetailsCheckbox()             //Клик чекбокс проверки корректности деталей операции
                .clickTransferButton()
                .checkAlertMessageAndAccept(TRANSFER_SUCCESS.format(transferAmount, account_2.accountNumber()))
                .shouldBeOpened()
                .refreshPage();

        //проверка баланса счета отправителя
        Double accountBalanceAfter_1 = accountBalanceBeforeTransfer_1 - transferAmount;
        transferPage.checkSenderAccountBalance(account_1.accountNumber(), accountBalanceAfter_1);
        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceAfter_1, within(0.001));

        //проверка баланса счета получателя
        Double accountBalanceAfter_2 = accountBalanceBeforeTransfer_2 + transferAmount;
        transferPage.checkReceivedAccountBalance(account_2.accountNumber(), accountBalanceAfter_2);
        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isEqualTo(accountBalanceAfter_2, within(0.001));
    }

    @Test
    public void userCanNotTransferOnNonExistentAccount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUsers.add(user);
        account_1 = UserSteps.createAndDepositAccount(user, depositAmount, 1);
        addAccountToAccountsIdsMap(user, account_1);
        int receiverAccountId = generateRandomAccountId();

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer = UserSteps.getBalance(user, account_1);
        String userName = RandomData.getUserName();

        //шаги
        authAsUser(user);

        TransferPage transferPage = dashboard.open()
                .clickTransferButton();            // юзер кликает на Make a Transfer

        transferPage.checkTransferHeader()                //Проверка элементов на странице
                .checkSelectAccountField()                 //Проверяем отображение поля
                .selectSenderAccount(account_1.accountNumber())      //Выбираем счет
                .inputRecipientName(userName)              // Вводим имя получателя
                .selectRecipientAccount(String.valueOf(receiverAccountId))   // Выбираем номер счета получателя
                .inputTransferAmount(transferAmount)
                .clickConfirmDetailsCheckbox()             //Клик чекбокс проверки корректности деталей операции
                .clickTransferButton()
                .checkAlertMessageAndAccept(ERROR_NO_USER_FOUND_WITH_THIS_ACCOUNT.getMessage())
                .shouldBeOpened()
                .refreshPage();

        //проверка баланса счета отправителя
        transferPage.checkSenderAccountBalance(account_1.accountNumber(), accountBalanceBeforeTransfer);
        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceBeforeTransfer, within(0.001));
    }

    @Test
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUsers.add(user);
        account_1 = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account_1);
        account_2 = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account_2);
        int receiverAccountId = generateRandomAccountId();

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);
        String userName = RandomData.getUserName();

        //шаги
        authAsUser(user);

        TransferPage transferPage = dashboard.open()
                .clickTransferButton();           // юзер кликает на Make a Transfer

        transferPage.checkTransferHeader()                //Проверка элементов на странице
                .checkSelectAccountField()                 //Проверяем отображение поля
                .selectSenderAccount(account_1.accountNumber())      //Выбираем счет
                .inputRecipientName(userName)              // Вводим имя получателя
                .selectRecipientAccount(String.valueOf(receiverAccountId))   // Выбираем номер счета получателя
                .inputTransferAmount(transferAmount)
                .clickConfirmDetailsCheckbox()             //Клик чекбокс проверки корректности деталей операции
                .clickTransferButton()
                .checkAlertMessageAndAccept(ERROR_INVALID_TRANSFER.getMessage())
                .shouldBeOpened()
                .refreshPage();

        //проверка баланса счета отправителя
        transferPage.checkSenderAccountBalance(account_1.accountNumber(), accountBalanceBeforeTransfer_1);
        Double accountBalanceAfterTransfer_1 = UserSteps.getBalance(user, account_1);
        assertThat(accountBalanceAfterTransfer_1).isEqualTo(accountBalanceBeforeTransfer_1, within(0.001));

        //проверка баланса счета получателя
        transferPage.checkReceivedAccountBalance(account_1.accountNumber(), accountBalanceBeforeTransfer_2);
        Double accountBalanceAfterTransfer_2 = UserSteps.getBalance(user, account_2);
        assertThat(accountBalanceAfterTransfer_2).isEqualTo(accountBalanceBeforeTransfer_2, within(0.001));
    }
    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAllAccounts(accountsIds);
        AdminSteps.deleteAllUsers(createdUsers);
    }
}
