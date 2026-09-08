package gr.aueb.cf.agriapp.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class ValidationException extends Exception {

    private final BindingResult bindingResult;

    public ValidationException(BindingResult bindingResult) {
        super("Η επικύρωση των δεδομένων απέτυχε");
        this.bindingResult = bindingResult;
    }
}
