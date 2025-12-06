package api.models.customer;

import api.models.BaseModel;
import api.models.accounts.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountsResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private Double balance;
    private List<Transaction> transactions;
}
