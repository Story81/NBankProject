package ui.pages;

import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositPage extends BasePage<DepositPage> {
    @Override
    public String url() {
        return "/deposit";
    }

    private final SelenideElement depositMoneyHeader = $(byText("💰 Deposit Money"));
    private final SelenideElement selectAccountFieldTitle = $(byText("Select Account:"));
    private final SelenideElement selectAccountField = $(".account-selector");
    private final SelenideElement amountFieldTitle = $(byText("Enter Amount:"));
    private final SelenideElement amountField = $(".deposit-input");
    private final SelenideElement depositButton = $(byText("\uD83D\uDCB5 Deposit"));

    public DepositPage clickSelectAccountField() {
        return StepLogger.log("Click select account field", () -> {
            selectAccountFieldTitle.shouldBe(visible);
            selectAccountField.shouldBe(visible).shouldHave(text("-- Choose an account --"));
            selectAccountField.click();
            return this;
        });
    }

    public DepositPage selectDepositAccount(String accountNumber) {
        return StepLogger.log(String.format("Select deposit account: %s", accountNumber), () -> {
            SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
            selectAccount.click();
            selectAccountField.shouldHave(text(accountNumber));
            return this;
        });
    }

    public DepositPage enterDepositAmountInField(Double depositAmount) {
        return StepLogger.log(String.format("Enter deposit amount: %.2f", depositAmount), () -> {
            amountFieldTitle.shouldBe(visible);
            amountFieldTitle.shouldBe(visible).shouldHave(text("Enter amount"));
            amountField.sendKeys(depositAmount.toString());
            return this;
        });
    }

    public DepositPage clickDepositButton() {
        return StepLogger.logWithScreenshotBefore("click Deposit Button", () -> {
        depositButton.shouldBe(visible).click();
        return this;
        });
    }

    public DepositPage checkAccountBalance(String accountNumber, Double depositAmount) {
        return StepLogger.log(String.format("Check account %s balance: %.2f", accountNumber, depositAmount), () -> {
            SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
            selectAccount.shouldHave(text(depositAmount.toString()));
            return this;
        });
    }
}
