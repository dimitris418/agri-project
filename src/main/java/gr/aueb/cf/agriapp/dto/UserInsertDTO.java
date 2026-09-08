package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserInsertDTO(

        @NotEmpty(message = "Το όνομα είναι υποχρεωτικό")
        @Size(min = 2, max = 50, message = "Το όνομα πρέπει να έχει από 2 έως 50 χαρακτήρες")
        String firstname,

        @NotEmpty(message = "Το επώνυμο είναι υποχρεωτικό")
        @Size(min = 2, max = 50, message = "Το επώνυμο πρέπει να έχει από 2 έως 50 χαρακτήρες")
        String lastname,

        @Email(message = "Μη έγκυρο όνομα χρήστη")
        @NotEmpty(message = "Το όνομα χρήστη είναι υποχρεωτικό")
        String username,

        @NotEmpty(message = "Το συνθηματικό είναι υποχρεωτικό")
        @Pattern(regexp = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)(?=.*?[@#$!%&*]).{8,}$",
                message = "Το συνθηματικό πρέπει να έχει τουλάχιστον 8 χαρακτήρες και να περιέχει πεζό, κεφαλαίο, ψηφίο και σύμβολο")
        String password,

        @NotEmpty(message = "Το ΑΦΜ είναι υποχρεωτικό")
        @Pattern(regexp = "\\d{9}", message = "Το ΑΦΜ πρέπει να είναι εννέα ψηφία")
        String vat
) {}
