package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.core.exceptions.*;
import gr.aueb.cf.agriapp.core.filters.FarmerFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.FarmerReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FarmerStatusUpdateDTO;
import gr.aueb.cf.agriapp.dto.FarmerUpdateDTO;
import gr.aueb.cf.agriapp.service.IFarmerService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
public class FarmerRestController {

    private final IFarmerService farmerService;

    @PostMapping
    public ResponseEntity<FarmerReadOnlyDTO> register(
            @Valid @RequestBody FarmerInsertDTO dto,
            BindingResult bindingResult)
            throws ValidationException, AppObjectAlreadyExists, AppServerException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        FarmerReadOnlyDTO created = farmerService.registerFarmer(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.uuid())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /** Διαχειριστική προβολή: απαιτεί MANAGE_USERS, βλ. SecurityConfiguration. */
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<Paginated<FarmerReadOnlyDTO>> search(@ModelAttribute FarmerFilters filters) {
        return ResponseEntity.ok(farmerService.getFarmersFilteredPaginated(filters));
    }

    /** Λογική διαγραφή και επαναφορά λογαριασμού. Απαιτεί MANAGE_USERS. */
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{uuid}/status")
    public ResponseEntity<FarmerReadOnlyDTO> setStatus(
            @PathVariable String uuid,
            @Valid @RequestBody FarmerStatusUpdateDTO dto,
            BindingResult bindingResult)
            throws ValidationException, AppObjectNotFoundException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        return ResponseEntity.ok(farmerService.setFarmerStatus(uuid, dto));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<FarmerReadOnlyDTO> getCurrentFarmer(Authentication authentication)
            throws AppObjectNotFoundException {

        return ResponseEntity.ok(farmerService.getFarmerByUsername(authentication.getName()));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public ResponseEntity<FarmerReadOnlyDTO> updateCurrentFarmer(
            @Valid @RequestBody FarmerUpdateDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException, AppObjectAlreadyExists {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        return ResponseEntity.ok(farmerService.updateFarmer(dto, authentication.getName()));
    }
}
