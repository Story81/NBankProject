package common.extensions;

import api.requests.steps.UserSteps;
import common.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.Account;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

@Order(2)
public class AccountExtension implements BeforeEachCallback, AfterEachCallback {


    @Override
    public void beforeEach(ExtensionContext context) {
        List<UserData> allUsers = SessionStorage.getAllUsers();

        Account annotation = context.getRequiredTestMethod().getAnnotation(Account.class);
        if (annotation != null) {
            if (SessionStorage.getAllUsers().isEmpty()) {
                throw new IllegalStateException(
                        "Users are not created.");
            }

            int totalUsers = annotation.user(); // сколько пользователей затронуть
            int accountsPerUser = annotation.value();    // сколько счетов на каждого необъодимо создать

            // Берём первых N пользователей
            List<UserData> targetUsers = allUsers.subList(0, totalUsers);

            for (UserData user : targetUsers) {
                for (int i = 0; i < accountsPerUser; i++) {
                    AccountData account = UserSteps.createAccount(user);
                    SessionStorage.addAccount(user, account);
                }
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Account annotation = context.getRequiredTestMethod().getAnnotation(Account.class);
        if (annotation != null) {
            // Удаляем все созданные аккаунты
            SessionStorage.deleteAllCreatedAccounts();
        }
    }
}
