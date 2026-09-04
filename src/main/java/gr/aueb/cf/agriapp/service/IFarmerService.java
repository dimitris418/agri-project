package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.exceptions.AppServerException;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.FarmerReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FarmerUpdateDTO;

public interface IFarmerService {

    FarmerReadOnlyDTO registerFarmer(FarmerInsertDTO farmerInsertDTO)
            throws AppObjectAlreadyExists, AppServerException;

    FarmerReadOnlyDTO getFarmerByUsername(String username)
            throws AppObjectNotFoundException;

    FarmerReadOnlyDTO updateFarmer(FarmerUpdateDTO farmerUpdateDTO, String username)
            throws AppObjectNotFoundException, AppObjectAlreadyExists;
}
