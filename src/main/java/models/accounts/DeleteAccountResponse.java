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
public class DeleteAccountResponse extends BaseModel {
    private String message;
    private Integer accountId;
}
