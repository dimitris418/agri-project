package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserInsertDTO(

        @NotEmpty(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstname,

        @NotEmpty(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastname,

        @Email(message = "Invalid username")
        @NotEmpty(message = "Username is required")
        String username,

        @NotEmpty(message = "Password is required")
        @Pattern(regexp = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)(?=.*?[@#$!%&*]).{8,}$",
                message = "Password must be at least 8 characters and contain a lowercase letter, an uppercase letter, a digit and a special character")
        String password,

        @NotEmpty(message = "VAT number is required")
        @Pattern(regexp = "\\d{9}", message = "VAT must be a 9-digit number")
        String vat
) {}
