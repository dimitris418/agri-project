package gr.aueb.cf.agriapp.core.filters;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Set;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FieldActivityFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String cropUuid;

    @Nullable
    private ActivityType type;

    @Nullable
    private LocalDate dateFrom;

    @Nullable
    private LocalDate dateTo;

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of("id", "activityDate", "type", "createdAt");
    }
}
