package common.storage;

import api.requests.steps.UserSteps;
import api.utils.AccountData;
import api.utils.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SessionStorage {
    /**
     * ThreadLocal - способ сделать SessionStorage потокобезопасным. Каждый поток получает свою копию
     * Map<Thread,SessionStorage>
     */
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage:: new);
    private final LinkedHashMap<UserData, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final Map<UserData, List<AccountData>> userAccountsMap = new HashMap<>();
    private final Map<UserData, List<Integer>> userAccountIdsMap = new HashMap<>();


    private SessionStorage() {
    }
    public static void addUsers(List<UserData>users) {
        for (UserData user: users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user));
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */
    public static UserData getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number - 1);
    }

    public static UserData getUser() {
        return getUser(1);
    }

    public static ArrayList<UserData> getAllUsers() {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet());
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }


    public static void addAccount(UserData user, AccountData account) {
        INSTANCE.get().userAccountsMap
                .computeIfAbsent(user, k -> new ArrayList<>())
                .add(account);

        INSTANCE.get().userAccountIdsMap
                .computeIfAbsent(user, k -> new ArrayList<>())
                .add(account.id());

    }

    public static AccountData getAccount(UserData user, int number) {
        return INSTANCE.get().userAccountsMap.get(user).get(number - 1);
    }

    public static AccountData getFirstAccount(UserData user) {
        return getAccount(user, 1);
    }

    public static void deleteAllCreatedAccounts() {
        INSTANCE.get().userAccountIdsMap.forEach((user, ids) -> {
            if (ids != null && !ids.isEmpty()) {
                UserSteps.deleteAllAccounts(Map.of(user, ids));
            }
        });

        INSTANCE.get().userAccountIdsMap.clear();
        clearAccounts();
    }

    public static void clearAccounts() {
        INSTANCE.get().userAccountsMap.clear();
    }
    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
        clearAccounts();
    }

    public static void printState() {
        System.out.println("=== SessionStorage State Dump ===");

        if (INSTANCE.get().userStepsMap.isEmpty()) {
            System.out.println("No users stored.");
            return;
        }

        int userIndex = 1;
        for (Map.Entry<UserData, UserSteps> entry : INSTANCE.get().userStepsMap.entrySet()) {
            UserData user = entry.getKey();
            List<AccountData> accounts = INSTANCE.get().userAccountsMap.getOrDefault(user, Collections.emptyList());
            List<Integer> accountIds = INSTANCE.get().userAccountIdsMap.getOrDefault(user, Collections.emptyList());

            System.out.printf("User #%d:%n", userIndex);
            System.out.printf("  - UserData: %s%n", user);
            System.out.printf("  - Accounts (%d):%n", accounts.size());

            if (accounts.isEmpty()) {
                System.out.println("      (no accounts)");
            } else {
                for (int i = 0; i < accounts.size(); i++) {
                    AccountData account = accounts.get(i);
                    Integer accountId = i < accountIds.size() ? accountIds.get(i) : null;
                    System.out.printf("      #%d: ID=%s, Account=%s%n",
                            i + 1,
                            accountId != null ? accountId : "unknown",
                            account);
                }
            }
            userIndex++;
            System.out.println();
        }
        System.out.println("=== End of SessionStorage State ===");
    }
}
