package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.core.exceptions.*;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.filters.ParcelFilters;
import gr.aueb.cf.agriapp.dto.ParcelInsertDTO;
import gr.aueb.cf.agriapp.dto.ParcelReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ParcelUpdateDTO;
import gr.aueb.cf.agriapp.service.IParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelRestController {

    private final IParcelService parcelService;

    @PostMapping
    public ResponseEntity<ParcelReadOnlyDTO> save(
            @Valid @RequestBody ParcelInsertDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException, AppObjectAlreadyExists {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        ParcelReadOnlyDTO created = parcelService.saveParcel(dto, authentication.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.uuid())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ParcelReadOnlyDTO> getOne(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        return ResponseEntity.ok(parcelService.getParcel(uuid, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Paginated<ParcelReadOnlyDTO>> search(
            @ModelAttribute ParcelFilters filters,
            Authentication authentication) throws AppObjectNotFoundException {

        return ResponseEntity.ok(parcelService.getParcelsFilteredPaginated(filters, authentication.getName()));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ParcelReadOnlyDTO> update(
            @PathVariable String uuid,
            @Valid @RequestBody ParcelUpdateDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException,
            AppObjectNotAuthorizedException, AppObjectAlreadyExists, AppObjectInvalidArgumentException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);
        assertPathMatchesBody(uuid, dto.uuid());

        return ResponseEntity.ok(parcelService.updateParcel(dto, authentication.getName()));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deactivate(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        parcelService.deactivateParcel(uuid, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private void assertPathMatchesBody(String pathUuid, String bodyUuid)
            throws AppObjectInvalidArgumentException {
        if (!pathUuid.equals(bodyUuid)) {
            throw new AppObjectInvalidArgumentException("Parcel",
                    "The uuid in the path does not match the uuid in the body");
        }
    }
}
