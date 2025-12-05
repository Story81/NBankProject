package api.models.accounts;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel {
    public static final String TRANSFER_SUCCESSFUL = "Transfer successful";
    private Integer receiverAccountId;
    private Double amount;
    private String message;
    private Integer senderAccountId;
}
