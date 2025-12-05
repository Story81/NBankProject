package models.admin;
import generatos.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.BaseModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "^user[A-Za-z0-9]{6,11}$")
    private String username;
    @GeneratingRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingRule(regex = "^USER$")
    private String role;

    public static CreateUserRequest getAdmin() {
        return CreateUserRequest.builder()
                .username(configs.Config.getProperty("admin.username"))
                .password(configs.Config.getProperty("admin.password"))
                .build();
    }

}
