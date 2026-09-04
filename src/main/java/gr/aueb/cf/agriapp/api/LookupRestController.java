package gr.aueb.cf.agriapp.api;

import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.PestType;
import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import gr.aueb.cf.agriapp.dto.CropTypeReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.PestReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ProductReadOnlyDTO;
import gr.aueb.cf.agriapp.service.ILookupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
public class LookupRestController {

    private final ILookupService lookupService;

    @GetMapping("/crop-types")
    public ResponseEntity<List<CropTypeReadOnlyDTO>> getCropTypes(
            @Nullable @RequestParam(required = false) CropSeason season) {
        return ResponseEntity.ok(lookupService.getCropTypes(season));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductReadOnlyDTO>> getProducts(
            @Nullable @RequestParam(required = false) ProductCategory category) {
        return ResponseEntity.ok(lookupService.getProducts(category));
    }

    @GetMapping("/pests")
    public ResponseEntity<List<PestReadOnlyDTO>> getPests(
            @Nullable @RequestParam(required = false) PestType type) {
        return ResponseEntity.ok(lookupService.getPests(type));
    }
}
