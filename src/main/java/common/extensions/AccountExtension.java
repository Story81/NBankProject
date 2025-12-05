package common.extensions;

import api.requests.steps.UserSteps;
import api.storage.SessionStorage;
import api.utils.AccountData;
import api.utils.UserData;
import common.annotations.Account;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.requests.steps.UserSteps.addAccountToAccountsIdsMap;

@Order(2)
public class AccountExtension implements BeforeEachCallback, AfterEachCallback {


    @Override
    public void beforeEach(ExtensionContext context) {
        if (SessionStorage.getAllUsers().isEmpty()) {
            throw new IllegalStateException(
                    "User must be created before account. Use @UserSession first.");
        }

        Account annotation = context.getRequiredTestMethod().getAnnotation(Account.class);
        if (annotation != null) {
            UserData user = SessionStorage.getUser(annotation.user());

            for (int i = 0; i < annotation.value(); i++) {
                AccountData account = UserSteps.createAccount(user);
                SessionStorage.addAccount(user, account); // автоматически сохраняет ID
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
