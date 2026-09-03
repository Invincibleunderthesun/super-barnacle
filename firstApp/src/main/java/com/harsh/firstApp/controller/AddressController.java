package com.harsh.firstApp.controller;

import com.harsh.firstApp.dto.ApiResponse;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.Address;
import com.harsh.firstApp.model.User;
import com.harsh.firstApp.repository.AddressRepository;
import com.harsh.firstApp.repository.UserRepository;
import com.harsh.firstApp.security.JwtFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "Addresses", description = "User shipping address management")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final JwtFilter jwtFilter;

    public AddressController(AddressRepository addressRepository,
            UserRepository userRepository,
            JwtFilter jwtFilter) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.jwtFilter = jwtFilter;
    }

    private User getCurrentUser() {
        Long userId = jwtFilter.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get all addresses for the current user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses() {
        User user = getCurrentUser();
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved", addresses));
    }

    @Operation(summary = "Get address by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> getAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success("Address retrieved", address));
    }

    @Operation(summary = "Create a new address")
    @PostMapping
    public ResponseEntity<ApiResponse<Address>> createAddress(@Valid @RequestBody Address address) {
        User user = getCurrentUser();
        address.setUser(user);

        // If this is the first address or marked as default, handle default logic
        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            // Unset current default
            existingAddresses.stream()
                    .filter(Address::isDefault)
                    .forEach(a -> {
                        a.setDefault(false);
                        addressRepository.save(a);
                    });
        }

        Address saved = addressRepository.save(address);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created", saved));
    }

    @Operation(summary = "Update an existing address")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody Address updatedAddress) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        address.setFullName(updatedAddress.getFullName());
        address.setPhone(updatedAddress.getPhone());
        address.setAddressLine1(updatedAddress.getAddressLine1());
        address.setAddressLine2(updatedAddress.getAddressLine2());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setPincode(updatedAddress.getPincode());

        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success("Address updated", saved));
    }

    @Operation(summary = "Set address as default")
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Address>> setDefaultAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        // Unset all current defaults
        addressRepository.findByUser(user).stream()
                .filter(Address::isDefault)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });

        address.setDefault(true);
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", saved));
    }

    @Operation(summary = "Delete an address")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        addressRepository.delete(address);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }
}
