package com.supplier.supplier.service;


import com.supplier.supplier.dto.Response;
import com.supplier.supplier.dto.SupplierDTO;
import com.supplier.supplier.exception.NotFoundException;
import com.supplier.supplier.model.Supplier;
import com.supplier.supplier.repository.SupplierRepository;
import com.supplier.supplier.service.impl.SupplierServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    private ModelMapper modelMapper;
    private SupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        supplierService = new SupplierServiceImpl(supplierRepository, modelMapper);
    }

    @Test
    void addSupplier_savesAndReturnsOk() {
        SupplierDTO dto = new SupplierDTO(null, "RMV Clothing", "9878904460", "Pune");
        Supplier saved = Supplier.builder()
                .id(1L)
                .name(dto.getName())
                .contactInfo(dto.getContactInfo())
                .address(dto.getAddress())
                .build();

        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        Response resp = supplierService.addSupplier(dto);

        assertEquals(200, resp.getStatus());
        assertEquals("Supplier Saved Successfully", resp.getMessage());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_existing_updatesAndReturnsOk() {
        Long id = 1L;
        Supplier existing = Supplier.builder()
                .id(id)
                .name("Old")
                .contactInfo("old@ex.com")
                .address("Old Addr")
                .build();

        SupplierDTO updateDto = new SupplierDTO(null, "NewName", "new@ex.com", "New Addr");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        Response resp = supplierService.updateSupplier(id, updateDto);

        assertEquals(200, resp.getStatus());
        assertEquals("Supplier Was Successfully Updated", resp.getMessage());
        assertEquals("NewName", existing.getName());
        assertEquals("new@ex.com", existing.getContactInfo());
        assertEquals("New Addr", existing.getAddress());
        verify(supplierRepository).save(existing);
    }

    @Test
    void updateSupplier_notFound_throwsNotFoundException() {
        Long id = 99L;
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> supplierService.updateSupplier(id, new SupplierDTO()));
    }

    @Test
    void getAllSupplier_returnsMappedList() {
        Supplier s = Supplier.builder().id(1L).name("Acme").contactInfo("c@a.com").address("Addr").build();
        when(supplierRepository.findAll(any(Sort.class))).thenReturn(List.of(s));

        Response resp = supplierService.getAllSupplier();

        assertEquals(200, resp.getStatus());
        assertNotNull(resp.getSuppliers());
        assertEquals(1, resp.getSuppliers().size());
        assertEquals("Acme", resp.getSuppliers().get(0).getName());
    }

    @Test
    void getSupplierById_existing_returnsMapped() {
        Long id = 1L;
        Supplier s = Supplier.builder().id(id).name("Acme").contactInfo("c@a.com").address("Addr").build();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(s));

        Response resp = supplierService.getSupplierById(id);

        assertEquals(200, resp.getStatus());
        assertNotNull(resp.getSupplier());
        assertEquals("Acme", resp.getSupplier().getName());
    }

    @Test
    void getSupplierById_notFound_throwsNotFoundException() {
        when(supplierRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> supplierService.getSupplierById(5L));
    }

    @Test
    void deleteSupplier_existing_deletesAndReturnsOk() {
        Long id = 2L;
        Supplier s = Supplier.builder().id(id).name("ToDel").contactInfo("x@x.com").address("addr").build();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(s));
        doNothing().when(supplierRepository).deleteById(id);

        Response resp = supplierService.deleteSupplier(id);

        assertEquals(200, resp.getStatus());
        assertEquals("Supplier Was Successfully Deleted", resp.getMessage());
        verify(supplierRepository).deleteById(id);
    }

    @Test
    void deleteSupplier_notFound_throwsNotFoundException() {
        Long id = 99L;
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> supplierService.deleteSupplier(id));
    }
}