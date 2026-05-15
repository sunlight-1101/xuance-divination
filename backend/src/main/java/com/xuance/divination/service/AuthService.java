package com.xuance.divination.service;

import com.xuance.divination.dto.LoginDTO;
import com.xuance.divination.dto.RegisterDTO;
import com.xuance.divination.dto.EmailCodeDTO;
import com.xuance.divination.dto.UserProfileDTO;
import com.xuance.divination.vo.UserVO;

public interface AuthService {
    Boolean sendRegisterEmailCode(EmailCodeDTO dto);
    UserVO register(RegisterDTO dto);
    UserVO login(LoginDTO dto);
    UserVO updateProfile(UserProfileDTO dto);
}
