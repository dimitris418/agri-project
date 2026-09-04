package gr.aueb.cf.agriapp.authentication;

import gr.aueb.cf.agriapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.agriapp.dto.AuthenticationResponseDTO;
import gr.aueb.cf.agriapp.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication.getName(), user.getRole().getName());

        return new AuthenticationResponseDTO(user.getFirstname(), user.getLastname(), token);
    }
}
