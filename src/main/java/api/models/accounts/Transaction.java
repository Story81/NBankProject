package api.models.accounts;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private Integer id;
    private Double amount;
    private String type;
    private String timestamp;
    private Integer relatedAccountId;
}
