package nBankTests.ui.iteration_2;

import com.codeborne.selenide.WebDriverRunner;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import ui.pages.UserDashboard;
import utils.AccountData;
import utils.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static generatos.RandomData.getDepositAmount;
import static nBankTests.api.iteration2_senior_level.TransferPositiveTest.addAccountToAccountsIdsMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static ui.pages.BankAlert.DEPOSIT_SUCCESS;
import static ui.pages.BankAlert.ERROR_ENTER_VALID_AMOUNT;
import static ui.pages.BankAlert.ERROR_SELECT_AN_ACCOUNT;

public class DepositTest extends BaseUiTest {
    UserDashboard dashboard = new UserDashboard();
    private static AccountData account;
    private static UserData user;
    private static List<UserData> createdUserIds = new ArrayList<>();
    private static Map<UserData, List<Integer>> accountsIds = new HashMap<>();


    @Test
    public void userCanDepositAndBalanceChangesCorrectlyTest() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUserIds.add(user);
        account = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();

        authAsUser(user);

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(accountNumber)
                .enterDepositAmountInField(depositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(DEPOSIT_SUCCESS.format(depositAmount, accountNumber));

        assertThat(WebDriverRunner.url()).contains(dashboard.url());

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(accountNumber, depositAmount);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isEqualTo(depositAmount, within(0.0001));

    }

    @Test
    public void userCanNotDepositWithEmptyAccount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUserIds.add(user);
        account = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account);
        String accountNumber = account.accountNumber();
        Double depositAmount = getDepositAmount();
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        authAsUser(user);

        dashboard.open()
                .clickDepositButton()
                .enterDepositAmountInField(depositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_SELECT_AN_ACCOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(accountNumber, accountBalanceBeforeDeposit);

        Double currentAccountBalance = UserSteps.getBalance(user, account);
        assertThat(currentAccountBalance).isZero();
    }

    @Test
    public void userCanNotDepositWithEmptyAmount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUserIds.add(user);
        account = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account);
        String accountNumber = account.accountNumber();
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        authAsUser(user);

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(accountNumber)
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_ENTER_VALID_AMOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(accountNumber, accountBalanceBeforeDeposit);

        Double currentAccountBalance = UserSteps.getBalance(user, account);
        assertThat(currentAccountBalance).isZero();
    }

    @Test
    public void userCanNotDepositWithInvalidAmount() {
        //создаем тестовые данные
        user = AdminSteps.createUser();
        createdUserIds.add(user);
        account = UserSteps.createAccount(user);
        addAccountToAccountsIdsMap(user, account);
        String accountNumber = account.accountNumber();
        Double invalidDepositAmount = getDepositAmount() * -1;
        Double accountBalanceBeforeDeposit = UserSteps.getBalance(user, account);

        authAsUser(user);

        dashboard.open()
                .clickDepositButton()
                .clickSelectAccountField()
                .selectDepositAccount(accountNumber)
                .enterDepositAmountInField(invalidDepositAmount)
                .clickDepositButton()
                .checkAlertMessageAndAccept(ERROR_ENTER_VALID_AMOUNT.getMessage())
                .shouldBeOpened();

        //проверка суммы на UI и через API
        dashboard.clickDepositButton()
                .checkAccountBalance(accountNumber, accountBalanceBeforeDeposit);

        Double accountBalance = UserSteps.getBalance(user, account);
        assertThat(accountBalance).isZero();
    }

    @AfterAll
    public static void deleteTestData() {
        UserSteps.deleteAllAccounts(accountsIds);
        AdminSteps.deleteAllUsers(createdUserIds);
    }
}
