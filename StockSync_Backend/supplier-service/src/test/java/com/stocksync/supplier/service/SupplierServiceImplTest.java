package com.stocksync.supplier.service;

import com.stocksync.supplier.dto.Response;
import com.stocksync.supplier.dto.SupplierDTO;
import com.stocksync.supplier.exception.NotFoundException;
import com.stocksync.supplier.model.Supplier;
import com.stocksync.supplier.repository.SupplierRepository;
import com.stocksync.supplier.service.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private SupplierDTO supplierDTO;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        supplierDTO = new SupplierDTO();
        supplierDTO.setName("Tata Pvt Ltd");
        supplierDTO.setContactInfo("9931116045");
        supplierDTO.setAddress("Jamshedpur");

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Tata Pvt Ltd");
        supplier.setContactInfo("9931116045");
        supplier.setAddress("Jamshedpur");
    }

    @Test
    void testAddSupplierSuccess() {
        when(modelMapper.map(supplierDTO, Supplier.class)).thenReturn(supplier);
        Response response = supplierService.addSupplier(supplierDTO);
        verify(supplierRepository, times(1)).save(supplier);
        assertEquals(200, response.getStatus());
        assertEquals("Supplier Saved Successfully", response.getMessage());
    }

    @Test
    void testUpdateSupplierSuccess() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        Response response = supplierService.updateSupplier(1L, supplierDTO);
        verify(supplierRepository, times(1)).save(supplier);
        assertEquals("Supplier Was Successfully Updated", response.getMessage());
    }

    @Test
    void testUpdateSupplierNotFound() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> supplierService.updateSupplier(1L, supplierDTO));
    }

    @Test
    void testGetSupplierByIdSuccess() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(modelMapper.map(supplier, SupplierDTO.class)).thenReturn(supplierDTO);
        Response response = supplierService.getSupplierById(1L);
        assertEquals(200, response.getStatus());
        assertEquals("success", response.getMessage());
        assertEquals("Tata Pvt Ltd", response.getSupplier().getName());
    }

    @Test
    void testGetSupplierByIdNotFound() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> supplierService.getSupplierById(1L));
    }

    @Test
    void testDeleteSupplierSuccess() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        Response response = supplierService.deleteSupplier(1L);
        verify(supplierRepository, times(1)).deleteById(1L);
        assertEquals("Supplier Was Successfully Deleted", response.getMessage());
    }

    @Test
    void testDeleteSupplierNotFound() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> supplierService.deleteSupplier(1L));
    }
}

