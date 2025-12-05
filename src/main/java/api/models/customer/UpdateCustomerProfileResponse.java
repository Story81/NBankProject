package api.models.customer;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerProfileResponse extends BaseModel {
    public static final String PROFILE_UPDATED_SUCCESSFULLY = "Profile updated successfully";
    private String message;
    private GetCustomerProfileResponse customer;
}
