package common.extensions;

import api.models.admin.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.storage.SessionStorage;
import api.utils.UserData;
import common.annotations.AdminSession;
import common.annotations.UserSession;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;
@Order(1)
public class UserSessionExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            SessionStorage.clear();
            List<UserData> users = new LinkedList<>();

            int userCount = annotation.value();
            for (int i = 0; i < userCount; i++) {
                UserData user = AdminSteps.createUser();
                users.add(user);
            }
            SessionStorage.addUsers(users);

            int authAsUser = annotation.auth();
            BasePage.authAsUser(SessionStorage.getUser(authAsUser));
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
           AdminSteps.deleteAllUsers(SessionStorage.getAllUsers());

            SessionStorage.clear();
        }
    }
}
