package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.core.exceptions.*;
import gr.aueb.cf.agriapp.core.filters.CropFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.dto.CropInsertDTO;
import gr.aueb.cf.agriapp.dto.CropReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.CropUpdateDTO;
import gr.aueb.cf.agriapp.service.ICropService;
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
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropRestController {

    private final ICropService cropService;

    @PostMapping
    public ResponseEntity<CropReadOnlyDTO> save(
            @Valid @RequestBody CropInsertDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException, AppObjectNotAuthorizedException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        CropReadOnlyDTO created = cropService.saveCrop(dto, authentication.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.uuid())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CropReadOnlyDTO> getOne(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        return ResponseEntity.ok(cropService.getCrop(uuid, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Paginated<CropReadOnlyDTO>> search(
            @ModelAttribute CropFilters filters,
            Authentication authentication) throws AppObjectNotFoundException {

        return ResponseEntity.ok(cropService.getCropsFilteredPaginated(filters, authentication.getName()));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CropReadOnlyDTO> update(
            @PathVariable String uuid,
            @Valid @RequestBody CropUpdateDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException,
            AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);
        if (!uuid.equals(dto.uuid())) {
            throw new AppObjectInvalidArgumentException("Crop",
                    "The uuid in the path does not match the uuid in the body");
        }

        return ResponseEntity.ok(cropService.updateCrop(dto, authentication.getName()));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        cropService.deleteCrop(uuid, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
