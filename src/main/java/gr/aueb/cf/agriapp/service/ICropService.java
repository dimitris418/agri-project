package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.CropFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.dto.CropInsertDTO;
import gr.aueb.cf.agriapp.dto.CropReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.CropUpdateDTO;

public interface ICropService {

    CropReadOnlyDTO saveCrop(CropInsertDTO cropInsertDTO, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    CropReadOnlyDTO updateCrop(CropUpdateDTO cropUpdateDTO, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    CropReadOnlyDTO getCrop(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    Paginated<CropReadOnlyDTO> getCropsFilteredPaginated(CropFilters filters, String username)
            throws AppObjectNotFoundException;

    void deleteCrop(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException;
}
