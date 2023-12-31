package com.example.officepcstore.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String id;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private String accessToken;
}
