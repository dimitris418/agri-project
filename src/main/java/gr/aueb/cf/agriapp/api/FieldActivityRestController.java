package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.core.exceptions.*;
import gr.aueb.cf.agriapp.core.filters.FieldActivityFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.dto.FieldActivityInsertDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityUpdateDTO;
import gr.aueb.cf.agriapp.service.IFieldActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class FieldActivityRestController {

    private final IFieldActivityService fieldActivityService;

    @PostMapping
    public ResponseEntity<FieldActivityReadOnlyDTO> save(
            @Valid @RequestBody FieldActivityInsertDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException,
            AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);

        FieldActivityReadOnlyDTO created = fieldActivityService.saveActivity(dto, authentication.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.uuid())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<FieldActivityReadOnlyDTO> getOne(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        return ResponseEntity.ok(fieldActivityService.getActivity(uuid, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Paginated<FieldActivityReadOnlyDTO>> search(
            @ModelAttribute FieldActivityFilters filters,
            Authentication authentication) throws AppObjectNotFoundException {

        return ResponseEntity.ok(
                fieldActivityService.getActivitiesFilteredPaginated(filters, authentication.getName()));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<FieldActivityReadOnlyDTO> update(
            @PathVariable String uuid,
            @Valid @RequestBody FieldActivityUpdateDTO dto,
            BindingResult bindingResult,
            Authentication authentication)
            throws ValidationException, AppObjectNotFoundException,
            AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult);
        if (!uuid.equals(dto.uuid())) {
            throw new AppObjectInvalidArgumentException("Activity",
                    "The uuid in the path does not match the uuid in the body");
        }

        return ResponseEntity.ok(fieldActivityService.updateActivity(dto, authentication.getName()));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid, Authentication authentication)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        fieldActivityService.deleteActivity(uuid, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
