package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.authentication.AuthenticationService;
import gr.aueb.cf.agriapp.core.exceptions.ValidationException;
import gr.aueb.cf.agriapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.agriapp.dto.AuthenticationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationService authenticationService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
            @Valid @RequestBody AuthenticationRequestDTO dto,
            BindingResult bindingResult) throws ValidationException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        return ResponseEntity.ok(authenticationService.authenticate(dto));
    }
}
