package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.interfaces.AddressInterface;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService implements AddressInterface {

    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {

        // maps address request data to address class
        Address address = modelMapper.map(addressDTO, Address.class);

        // get user addresses
        List<Address> addressList = user.getAddresses();

        // add new address to user address list
        addressList.add(address);

        // set new instance of adresses
        user.setAddresses(addressList);

        address.setUser(user);

        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {

        List<Address> addressList = addressRepository.findAll();

        List<AddressDTO> addressDTOS = addressList.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();

        return addressDTOS;

    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId.toString()));

        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);

        return addressDTO;
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addressList = user.getAddresses();

        List<AddressDTO> addressDTOS = addressList.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();

        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address addressFromDb = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId.toString()));

        addressFromDb.setCity(addressDTO.getCity());
        addressFromDb.setCountry(addressDTO.getCountry());
        addressFromDb.setStreet(addressDTO.getStreet());
        addressFromDb.setPincode(addressDTO.getPincode());
        addressFromDb.setBuildingName(addressDTO.getBuildingName());
        addressFromDb.setState(addressDTO.getState());

        Address updatedAddress = addressRepository.save(addressFromDb);

        // Update users address list

        User user = addressFromDb.getUser();
        // remove the old address object
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        // add the new address
        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {

        Address addressFromDb = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId.toString()));

        User user= addressFromDb.getUser();

        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));

        userRepository.save(user);

        addressRepository.delete(addressFromDb);

        return "Address deleted successfully";
    }
}
