package utils;

import models.admin.CreateUserResponse;

public record UserData(String username, String password, int id, String authHeader, String name, String role) {
    public static final String UNUSED = "UNUSED";
    public static UserData createFrom(CreateUserResponse response) {
        return new UserData(
                response.getUsername(),
                UNUSED,
                response.getId(),
                UNUSED,
                response.getName(),
                response.getRole()
        );
    }
}
