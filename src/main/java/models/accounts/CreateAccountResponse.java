package models.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.BaseModel;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel {
    private Integer id;
    private String accountNumber;
    private Double balance;
    private List<Transaction> transactions;
}
