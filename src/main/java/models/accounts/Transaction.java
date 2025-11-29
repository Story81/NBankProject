package models.accounts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import models.BaseModel;

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
