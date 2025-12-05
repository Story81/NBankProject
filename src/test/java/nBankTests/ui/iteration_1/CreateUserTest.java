package nBankTests.ui.iteration_1;

import api.generatos.RandomModelGenerator;
import api.models.admin.CreateUserRequest;
import api.models.admin.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import common.annotations.AdminSession;
import common.extensions.AdminSessionExtension;
import nBankTests.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.pages.AdminPanel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ui.pages.BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS;
import static ui.pages.BankAlert.USER_CREATED_SUCCESSFULLY;

@ExtendWith(AdminSessionExtension.class)
public class CreateUserTest extends BaseUiTest {
    private String invalidName = "a";

    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers().stream().anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername(invalidName);

        assertTrue(new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).count();
        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
