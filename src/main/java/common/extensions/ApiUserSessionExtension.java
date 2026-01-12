package common.extensions;

import api.requests.steps.AdminSteps;
import api.utils.UserData;
import common.annotations.ApiUserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

@Order(1)
public class ApiUserSessionExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext ctx) {
        ApiUserSession ann = ctx.getTestMethod().orElseThrow().getAnnotation(ApiUserSession.class);
        if (ann == null) {
            return;
        }

        SessionStorage.clear();
        List<UserData> users = new ArrayList<>();
        for (int i = 0; i < ann.value(); i++) {
            users.add(AdminSteps.createUser()); // ← только API
        }
        SessionStorage.addUsers(users);

    }

    @Override
    public void afterEach(ExtensionContext ctx) {
        // удаляем через API
        AdminSteps.deleteAllUsers(SessionStorage.getAllUsers());
        SessionStorage.clear();
    }
}
