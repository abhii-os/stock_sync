package com.stocksync.supplier.app.service.api;

import com.stocksync.supplier.app.dto.UserRegistrationRequestDTO;
import com.stocksync.supplier.app.dto.UserRegistrationResponseDTO;

public interface UserMgmtService {

    public UserRegistrationResponseDTO registerNewUser(UserRegistrationRequestDTO userReqDTO);

    public  boolean isNewUser(String userName);
}
