package org.example.bankingapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username bắt buộc")
    @Size(min = 4, max = 50, message = "Username phải có độ dài từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Password bắt buộc")
    @Size(min = 8, message = "Password phải có độ dài từ 8 ký tự trở lên")
    private String password;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên đầy đủ là bắt buộc")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có từ 10 đến 11 chữ số")
    private String phone;

    private LocalDate dateOfBirth;

    private String address;

    @NotBlank(message = "Cần có mã PIN")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã PIN phải có chính xác 6 chữ số")
    private String transactionPin;
}
