package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.FieldActivityFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.dto.FieldActivityInsertDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityUpdateDTO;

public interface IFieldActivityService {

    FieldActivityReadOnlyDTO saveActivity(FieldActivityInsertDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException;

    FieldActivityReadOnlyDTO updateActivity(FieldActivityUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException;

    FieldActivityReadOnlyDTO getActivity(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    Paginated<FieldActivityReadOnlyDTO> getActivitiesFilteredPaginated(FieldActivityFilters filters, String username)
            throws AppObjectNotFoundException;

    void deleteActivity(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
}
