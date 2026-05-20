package com.stocksync.userservice.service.api;

import com.stocksync.userservice.dto.UserRegistrationRequestDTO;
import com.stocksync.userservice.dto.UserRegistrationResponseDTO;

public interface UserMgmtService {

    public UserRegistrationResponseDTO registerNewUser(UserRegistrationRequestDTO userReqDTO);

    public  boolean isNewUser(String userName);
}
