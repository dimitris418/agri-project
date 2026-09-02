package gr.aueb.cf.agriapp.core.filters;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CropFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String parcelUuid;

    @Nullable
    private Long cropTypeId;

    @Nullable
    private Integer cultivationYear;

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of("id", "cultivationYear", "variety", "plantingDate", "expectedHarvestDate", "createdAt");
    }
}
