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
public class FarmerFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String lastname;

    @Nullable
    private String username;

    @Nullable
    private String registryNumber;

    @Nullable
    private Boolean active;

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of("id", "registryNumber", "user.lastname", "user.username", "createdAt");
    }
}
