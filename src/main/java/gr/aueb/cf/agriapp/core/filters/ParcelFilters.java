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
public class ParcelFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String name;

    @Nullable
    private String kaek;

    @Nullable
    private Boolean active;

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of("id", "name", "kaek", "areaInStremmas", "createdAt");
    }
}
