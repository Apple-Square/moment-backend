package applesquare.moment.address.service;

import applesquare.moment.address.dto.AddressSearchResponseDTO;

public interface AddressService {
    AddressSearchResponseDTO searchAddress(String keyword);
}
