package nBankTests.ui.iteration_2;

import api.requests.steps.UserSteps;
import common.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import com.codeborne.selenide.WebDriverRunner;
import common.annotations.Account;
import common.annotations.UserSession;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.UserDashboard;

import static api.generatos.RandomData.getDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static ui.pages.BankAlert.DEPOSIT_SUCCESS;
import static ui.pages.BankAlert.ERROR_ENTER_VALID_AMOUNT;
import static ui.pages.BankAlert.ERROR_SELECT_AN_ACCOUNT;

public class DepositTest extends BaseUiTest {
    UserDashboard dashboard = new UserDashboard();

    @Test
    @UserSession
    @Account
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getFirstAccount(user);
        Double depositAmount = getDepositAmount();

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(account.accountNumber())
                .enterDepositAmountInField(depositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(DEPOSIT_SUCCESS.format(depositAmount, account.accountNumber()));

        assertThat(WebDriverRunner.url()).contains(dashboard.url());

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(account.accountNumber(), depositAmount);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isEqualTo(depositAmount, within(0.001));

    }

    @Test
    @UserSession
    @Account
    public void userCanNotDepositWithEmptyAccount() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getFirstAccount(user);
        Double depositAmount = getDepositAmount();
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        dashboard.open()
                .clickDepositButton()
                .enterDepositAmountInField(depositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_SELECT_AN_ACCOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(account.accountNumber(), accountBalanceBeforeDeposit);

        Double currentAccountBalance = UserSteps.getBalance(user, account);
        assertThat(currentAccountBalance).isZero();
    }

    @Test
    @UserSession
    @Account
    public void userCanNotDepositWithEmptyAmount() {
        UserData user = SessionStorage.getUser();
        AccountData account = SessionStorage.getFirstAccount(user);
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(account.accountNumber())
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_ENTER_VALID_AMOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(account.accountNumber(), accountBalanceBeforeDeposit);

        Double currentAccountBalance = UserSteps.getBalance(user, account);
        assertThat(currentAccountBalance).isZero();
    }

    @Test
    @UserSession
    @Account
    public void userCanNotDepositWithInvalidAmount() {
        UserData user = SessionStorage.getUser();
        AccountData  account = SessionStorage.getFirstAccount(user);
        Double invalidDepositAmount = getDepositAmount() * -1;
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(account.accountNumber())
                .enterDepositAmountInField(invalidDepositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_ENTER_VALID_AMOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(account.accountNumber(), accountBalanceBeforeDeposit);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isZero();
    }
}
