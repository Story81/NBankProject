package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
@Getter
public class UserDashboard  extends BasePage<UserDashboard> {
    public static final String DEFAULT_NAME = "noname";
    private DepositPage depositPage = new DepositPage();
    private TransferPage transferPage = new TransferPage();
    private SelenideElement welcomeHeader = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(byText("➕ Create New Account"));
    private SelenideElement depositMoneyButton = $(byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferMoneyButton = $(byText("\uD83D\uDD04 Make a Transfer"));



    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard checkWelcomeText(String name) {
        welcomeHeader.shouldBe(visible).shouldHave(text("Welcome, " + name + "!"));
        return this;
    }

    public DepositPage clickDepositButton() {
        depositMoneyButton.shouldBe(visible).click();
        return depositPage;
    }

    public TransferPage clickTransferButton() {
        transferMoneyButton.shouldBe(visible).click();
        return transferPage;
    }
}
