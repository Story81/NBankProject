package api.models;

import lombok.Getter;

@Getter
public enum BankAlert {

    INVALID_TRANSFER_ERROR_MESSAGE("Invalid transfer: insufficient funds or invalid accounts"),
    UNAUTHORIZED_ACCESS_ERROR("Unauthorized access to account"),
    UNAUTHORIZED_ERROR_VALUE("Unauthorized access to account");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
