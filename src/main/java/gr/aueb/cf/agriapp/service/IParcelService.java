package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.filters.ParcelFilters;
import gr.aueb.cf.agriapp.dto.ParcelInsertDTO;
import gr.aueb.cf.agriapp.dto.ParcelReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ParcelUpdateDTO;

public interface IParcelService {

    ParcelReadOnlyDTO saveParcel(ParcelInsertDTO parcelInsertDTO, String username)
            throws AppObjectNotFoundException, AppObjectAlreadyExists;

    ParcelReadOnlyDTO updateParcel(ParcelUpdateDTO parcelUpdateDTO, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectAlreadyExists;

    ParcelReadOnlyDTO getParcel(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    Paginated<ParcelReadOnlyDTO> getParcelsFilteredPaginated(ParcelFilters filters, String username)
            throws AppObjectNotFoundException;

    void deactivateParcel(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
}
