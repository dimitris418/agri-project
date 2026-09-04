package gr.aueb.cf.agriapp.mapper;

import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.*;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import gr.aueb.cf.agriapp.model.static_data.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final PasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------- User
    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return UserReadOnlyDTO.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .vat(user.getVat())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }

    // --------------------------------------------------------------- Farmer
    public Farmer mapToFarmerEntity(FarmerInsertDTO dto) {
        UserInsertDTO userDTO = dto.userInsertDTO();

        User user = new User();
        user.setFirstname(userDTO.firstname());
        user.setLastname(userDTO.lastname());
        user.setUsername(userDTO.username());
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setVat(userDTO.vat());
        user.setIsActive(true);

        Farmer farmer = new Farmer();
        farmer.setRegistryNumber(dto.registryNumber());
        farmer.setPhone(dto.phone());
        farmer.setIsActive(true);
        farmer.setUser(user);

        return farmer;
    }

    public Farmer mapToFarmerEntity(FarmerUpdateDTO dto) {
        UserUpdateDTO userDTO = dto.userUpdateDTO();

        User user = new User();
        user.setId(userDTO.id());
        user.setFirstname(userDTO.firstname());
        user.setLastname(userDTO.lastname());
        user.setUsername(userDTO.username());
        if (userDTO.password() != null && !userDTO.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.password()));
        }
        user.setVat(userDTO.vat());
        user.setIsActive(dto.isActive());

        Farmer farmer = new Farmer();
        farmer.setId(dto.id());
        farmer.setUuid(dto.uuid());
        farmer.setRegistryNumber(dto.registryNumber());
        farmer.setPhone(dto.phone());
        farmer.setIsActive(dto.isActive());
        farmer.setUser(user);

        return farmer;
    }

    public FarmerReadOnlyDTO mapToFarmerReadOnlyDTO(Farmer farmer) {
        return FarmerReadOnlyDTO.builder()
                .id(farmer.getId())
                .uuid(farmer.getUuid())
                .registryNumber(farmer.getRegistryNumber())
                .phone(farmer.getPhone())
                .isActive(farmer.getIsActive())
                .userReadOnlyDTO(mapToUserReadOnlyDTO(farmer.getUser()))
                .build();
    }

    // --------------------------------------------------------------- Parcel
    public Parcel mapToParcelEntity(ParcelInsertDTO dto, Farmer farmer) {
        Parcel parcel = new Parcel();
        parcel.setName(dto.name());
        parcel.setLocation(dto.location());
        parcel.setAreaInStremmas(dto.areaInStremmas());
        parcel.setKaek(blankToNull(dto.kaek()));
        parcel.setIsActive(dto.isActive());
        parcel.setFarmer(farmer);
        return parcel;
    }

    public Parcel mapToParcelEntity(ParcelUpdateDTO dto, Farmer farmer) {
        Parcel parcel = new Parcel();
        parcel.setId(dto.id());
        parcel.setUuid(dto.uuid());
        parcel.setName(dto.name());
        parcel.setLocation(dto.location());
        parcel.setAreaInStremmas(dto.areaInStremmas());
        parcel.setKaek(blankToNull(dto.kaek()));
        parcel.setIsActive(dto.isActive());
        parcel.setFarmer(farmer);
        return parcel;
    }

    public ParcelReadOnlyDTO mapToParcelReadOnlyDTO(Parcel parcel) {
        return ParcelReadOnlyDTO.builder()
                .id(parcel.getId())
                .uuid(parcel.getUuid())
                .name(parcel.getName())
                .location(parcel.getLocation())
                .areaInStremmas(parcel.getAreaInStremmas())
                .kaek(parcel.getKaek())
                .isActive(parcel.getIsActive())
                .build();
    }

    // ----------------------------------------------------------------- Crop
    public Crop mapToCropEntity(CropInsertDTO dto, Parcel parcel, CropType cropType) {
        Crop crop = new Crop();
        crop.setCropType(cropType);
        crop.setVariety(dto.variety());
        crop.setCultivationYear(dto.cultivationYear());
        crop.setPlantingDate(dto.plantingDate());
        crop.setExpectedHarvestDate(dto.expectedHarvestDate());
        crop.setParcel(parcel);
        return crop;
    }

    public Crop mapToCropEntity(CropUpdateDTO dto, Parcel parcel, CropType cropType) {
        Crop crop = new Crop();
        crop.setId(dto.id());
        crop.setUuid(dto.uuid());
        crop.setCropType(cropType);
        crop.setVariety(dto.variety());
        crop.setCultivationYear(dto.cultivationYear());
        crop.setPlantingDate(dto.plantingDate());
        crop.setExpectedHarvestDate(dto.expectedHarvestDate());
        crop.setParcel(parcel);
        return crop;
    }

    public CropReadOnlyDTO mapToCropReadOnlyDTO(Crop crop, LocalDate harvestDate) {
        return CropReadOnlyDTO.builder()
                .id(crop.getId())
                .uuid(crop.getUuid())
                .cropTypeReadOnlyDTO(mapToCropTypeReadOnlyDTO(crop.getCropType()))
                .variety(crop.getVariety())
                .cultivationYear(crop.getCultivationYear())
                .plantingDate(crop.getPlantingDate())
                .expectedHarvestDate(crop.getExpectedHarvestDate())
                .harvestDate(harvestDate)
                .build();
    }

    // -------------------------------------------------------- FieldActivity
    public FieldActivity mapToFieldActivityEntity(FieldActivityInsertDTO dto,
                                                  Crop crop,
                                                  Product product,
                                                  Pest pest) {
        FieldActivity activity = new FieldActivity();
        activity.setActivityDate(dto.activityDate());
        activity.setType(dto.type());
        activity.setProduct(product);
        activity.setQuantity(dto.quantity());
        activity.setUnit(dto.unit());
        activity.setPest(pest);
        activity.setSeverity(dto.severity());
        activity.setNotes(dto.notes());
        activity.setCrop(crop);
        return activity;
    }

    public FieldActivity mapToFieldActivityEntity(FieldActivityUpdateDTO dto,
                                                  Crop crop,
                                                  Product product,
                                                  Pest pest) {
        FieldActivity activity = new FieldActivity();
        activity.setId(dto.id());
        activity.setUuid(dto.uuid());
        activity.setActivityDate(dto.activityDate());
        activity.setType(dto.type());
        activity.setProduct(product);
        activity.setQuantity(dto.quantity());
        activity.setUnit(dto.unit());
        activity.setPest(pest);
        activity.setSeverity(dto.severity());
        activity.setNotes(dto.notes());
        activity.setCrop(crop);
        return activity;
    }

    public FieldActivityReadOnlyDTO mapToFieldActivityReadOnlyDTO(FieldActivity activity) {
        return FieldActivityReadOnlyDTO.builder()
                .id(activity.getId())
                .uuid(activity.getUuid())
                .activityDate(activity.getActivityDate())
                .type(name(activity.getType()))
                .productReadOnlyDTO(mapToProductReadOnlyDTO(activity.getProduct()))
                .quantity(activity.getQuantity())
                .unit(name(activity.getUnit()))
                .pestReadOnlyDTO(mapToPestReadOnlyDTO(activity.getPest()))
                .severity(name(activity.getSeverity()))
                .notes(activity.getNotes())
                .build();
    }

    // -------------------------------------------------------------- Lookups
    public CropTypeReadOnlyDTO mapToCropTypeReadOnlyDTO(CropType cropType) {
        if (cropType == null) return null;
        return CropTypeReadOnlyDTO.builder()
                .id(cropType.getId())
                .name(cropType.getName())
                .latinName(cropType.getLatinName())
                .season(name(cropType.getSeason()))
                .build();
    }

    public ProductReadOnlyDTO mapToProductReadOnlyDTO(Product product) {
        if (product == null) return null;
        return ProductReadOnlyDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .activeSubstance(product.getActiveSubstance())
                .category(name(product.getCategory()))
                .preHarvestIntervalDays(product.getPreHarvestIntervalDays())
                .build();
    }

    public PestReadOnlyDTO mapToPestReadOnlyDTO(Pest pest) {
        if (pest == null) return null;
        return PestReadOnlyDTO.builder()
                .id(pest.getId())
                .name(pest.getName())
                .latinName(pest.getLatinName())
                .type(name(pest.getType()))
                .build();
    }

    // -------------------------------------------------------------- Helpers
    private String name(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
