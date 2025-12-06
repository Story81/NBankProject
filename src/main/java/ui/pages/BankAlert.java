package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SUCCESS("✅ Successfully deposited $%s to account %s!"),
    TRANSFER_SUCCESS("✅ Successfully transferred $%s to account %s!"),

    ERROR_SELECT_AN_ACCOUNT("❌ Please select an account."),
    ERROR_ENTER_VALID_AMOUNT("❌ Please enter a valid amount."),
    ERROR_NO_USER_FOUND_WITH_THIS_ACCOUNT("❌ No user found with this account number."),
    ERROR_INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    NAME_UPDATE_USER_CREATED_SUCCESSFULLY("✅ Name updated successfully!"),
    ERROR_ENTER_VALID_NAME("❌ Please enter a valid name."),
    ERROR_NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only");


    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
