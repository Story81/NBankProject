package ui.pages;

import api.models.admin.CreateUserRequest;
import api.specs.RequestSpecs;
import api.utils.UserData;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement homeButton = $(byText("\uD83C\uDFE0 Home"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T shouldBeOpened() {
        assertThat(WebDriverRunner.url()).contains(url());
        return (T) this;
    }

    public T refreshPage() {
        Selenide.refresh();
        shouldBeOpened();
        return (T) this;
    }

    public UserDashboard clickHomeButton() {
        UserDashboard userDashboard = new UserDashboard();
        homeButton.shouldBe(visible).click();
        return userDashboard;
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }
    public static void authAsAdmin(CreateUserRequest request) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(request.getUsername(), request.getPassword());
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }
    public static void authAsUser(UserData user) {
        authAsUser(user.username(), user.password());
    }

    //ElementsCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }

}
