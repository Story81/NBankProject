package nBankTests.ui.iteration_1;

import com.codeborne.selenide.Condition;
import generatos.RandomModelGenerator;
import models.admin.CreateUserRequest;
import models.admin.CreateUserResponse;
import models.comparison.ModelAssertions;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Condition.exactText;
import static org.assertj.core.api.Assertions.assertThat;
import static ui.pages.BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS;

public class CreateUserTest extends BaseUiTest {

    @Test
    public void adminCanCreateUserTest() {
        // ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin.getUsername(),admin.getPassword());

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        // ШАГ 5: проверка, что юзер создан на API
        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin.getUsername(),admin.getPassword());

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().findBy(exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        //Проверка, что юзер не найден
        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).count();
        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
