package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.PestType;
import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import gr.aueb.cf.agriapp.dto.CropTypeReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.PestReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ProductReadOnlyDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.repository.CropTypeRepository;
import gr.aueb.cf.agriapp.repository.PestRepository;
import gr.aueb.cf.agriapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupService implements ILookupService {

    private final CropTypeRepository cropTypeRepository;
    private final ProductRepository productRepository;
    private final PestRepository pestRepository;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CropTypeReadOnlyDTO> getCropTypes(CropSeason season) {
        var types = (season == null) ? cropTypeRepository.findAll() : cropTypeRepository.findBySeason(season);
        return types.stream().map(mapper::mapToCropTypeReadOnlyDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReadOnlyDTO> getProducts(ProductCategory category) {
        var products = (category == null) ? productRepository.findAll() : productRepository.findByCategory(category);
        return products.stream().map(mapper::mapToProductReadOnlyDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PestReadOnlyDTO> getPests(PestType type) {
        var pests = (type == null) ? pestRepository.findAll() : pestRepository.findByType(type);
        return pests.stream().map(mapper::mapToPestReadOnlyDTO).toList();
    }
}
