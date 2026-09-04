package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.PestType;
import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import gr.aueb.cf.agriapp.dto.CropTypeReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.PestReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ProductReadOnlyDTO;

import java.util.List;

public interface ILookupService {

    List<CropTypeReadOnlyDTO> getCropTypes(CropSeason season);

    List<ProductReadOnlyDTO> getProducts(ProductCategory category);

    List<PestReadOnlyDTO> getPests(PestType type);
}
