package ui.pages;

import com.codeborne.selenide.SelenideElement;
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
        selectAccountFieldTitle.shouldBe(visible);
        selectAccountField.shouldBe(visible).shouldHave(text("-- Choose an account --"));
        selectAccountField.click();
        return this;
    }

    public DepositPage selectDepositAccount(String accountNumber) {
        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
        selectAccount.click();
        selectAccountField.shouldHave(text(accountNumber));
        return this;
    }

    public DepositPage enterDepositAmountInField(Double depositAmount) {
        amountFieldTitle.shouldBe(visible);
        amountFieldTitle.shouldBe(visible).shouldHave(text("Enter amount"));
        amountField.sendKeys(depositAmount.toString());
        return this;
    }

    public DepositPage clickDepositButton() {
        depositButton.shouldBe(visible).click();
        return this;
    }

    public DepositPage checkAccountBalance(String accountNumber, Double depositAmount) {
        SelenideElement selectAccount = selectAccountField.$(byText(accountNumber));
        selectAccount.shouldHave(text(depositAmount.toString()));
        return this;
    }


}
