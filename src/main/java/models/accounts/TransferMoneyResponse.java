package models.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.BaseModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel {
    private Integer receiverAccountId;
    private Double amount;
    private String message;
    private Integer senderAccountId;
}
