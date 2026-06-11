package org.example.bankingapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @Email(message = "Email không đúng định dạng")
    private String email;
    private String fullName;
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải 10 số")
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean enabled;
}
