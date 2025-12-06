package api.utils;

import api.models.accounts.CreateAccountResponse;
import api.models.accounts.Transaction;
import api.models.customer.GetAccountsResponse;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

public record AccountData(Integer id, String accountNumber, double balance,    List<Transaction> transactions) {

    // Конструктор из CreateAccountResponse
    public AccountData(CreateAccountResponse response) {
        this(response.getId(), response.getAccountNumber(), response.getBalance(), response.getTransactions());
    }

    // Конструктор из GetAccountsResponse
    public AccountData(GetAccountsResponse response) {
        this(response.getId(), response.getAccountNumber(), response.getBalance(), response.getTransactions());
    }

    // Метод для проверки нового пустого счета
    public void assertIsValidNewAccount(SoftAssertions softly) {
        softly.assertThat(id).isPositive();
        softly.assertThat(accountNumber).isNotEmpty();
        softly.assertThat(balance).isEqualTo(0.0);
        softly.assertThat(transactions).isEmpty();
    }
}
