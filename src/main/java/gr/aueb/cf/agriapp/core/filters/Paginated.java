package gr.aueb.cf.agriapp.core.filters;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
public class Paginated<T> {

    List<T> data;
    int currentPage;
    int pageSize;
    int totalPages;
    int numberOfElements;
    long totalElements;

    public static <T> Paginated<T> fromPage(Page<T> page) {
        return Paginated.<T>builder()
                .data(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .totalElements(page.getTotalElements())
                .build();
    }
}
