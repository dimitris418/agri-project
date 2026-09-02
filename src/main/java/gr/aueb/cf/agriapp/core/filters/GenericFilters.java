package gr.aueb.cf.agriapp.core.filters;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Getter
@Setter
public abstract class GenericFilters {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_SORT_COLUMN = "id";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;

    private int page;
    private int pageSize;
    private String sortBy;
    private Sort.Direction sortDirection;

    public int getPage() {
        return Math.max(page, 0);
    }

    public int getPageSize() {
        return pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    protected abstract Set<String> getAllowedSortColumns();

    public String getSortBy() {
        if (this.sortBy == null || this.sortBy.isBlank()) return DEFAULT_SORT_COLUMN;
        return getAllowedSortColumns().contains(this.sortBy) ? this.sortBy : DEFAULT_SORT_COLUMN;
    }

    public Sort.Direction getSortDirection() {
        return this.sortDirection != null ? this.sortDirection : DEFAULT_SORT_DIRECTION;
    }

    public Pageable getPageable() {
        return PageRequest.of(getPage(), getPageSize(), getSort());
    }

    public Sort getSort() {
        return Sort.by(this.getSortDirection(), this.getSortBy());
    }
}
