package api.models.accounts;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositMoneyResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private Double balance;
    private List<Transaction> transactions;
}
