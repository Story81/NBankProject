package nBankTests.ui.iteration_2;

import api.generatos.RandomData;
import common.storage.SessionStorage;
import common.annotations.Account;
import common.annotations.UserSession;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;
import api.utils.AccountData;
import api.utils.UserData;

import static api.generatos.RandomData.generateRandomAccountId;
import static api.generatos.RandomData.getDepositAmount;
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

    @Test
    @UserSession
    @Account(value=2)
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        user = SessionStorage.getUser();
        account_1 = SessionStorage.getAccount(user,1);
        UserSteps.deposit(user, account_1, depositAmount);
        account_2 = SessionStorage.getAccount(user,2);

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);
        String userName = RandomData.getUserName();

        //шаги
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
    @UserSession
    @Account(value=2)
    public void userCanDepositWithEmptyRecipientNameTest() {
        user = SessionStorage.getUser();
        account_1 = SessionStorage.getAccount(user,1);
        UserSteps.deposit(user, account_1, depositAmount);
        account_2 = SessionStorage.getAccount(user,2);

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);

        //шаги
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
    @UserSession
    @Account
    public void userCanNotTransferOnNonExistentAccount() {
        user = SessionStorage.getUser();
        account_1 = SessionStorage.getAccount(user,1);
        UserSteps.deposit(user, account_1, depositAmount);
        int receiverAccountId = generateRandomAccountId();

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer = UserSteps.getBalance(user, account_1);
        String userName = RandomData.getUserName();

        //шаги
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
    @UserSession
    @Account(value=2)
    public void userCanNotTransferWhenTransferAmountExceedsAccountBalance() {
        user = SessionStorage.getUser();
        account_1 = SessionStorage.getAccount(user,1);
        account_2 = SessionStorage.getAccount(user,2);

        Double transferAmount = getDepositAmount();
        Double accountBalanceBeforeTransfer_1 = UserSteps.getBalance(user, account_1);
        Double accountBalanceBeforeTransfer_2 = UserSteps.getBalance(user, account_2);
        String userName = RandomData.getUserName();

        //шаги
        TransferPage transferPage = dashboard.open()
                .clickTransferButton();           // юзер кликает на Make a Transfer

        transferPage.checkTransferHeader()                //Проверка элементов на странице
                .checkSelectAccountField()                 //Проверяем отображение поля
                .selectSenderAccount(account_1.accountNumber())      //Выбираем счет
                .inputRecipientName(userName)              // Вводим имя получателя
                .selectRecipientAccount(String.valueOf(account_2.accountNumber()))   // Выбираем номер счета получателя
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
}
