package com.xuance.divination.service;

import com.xuance.divination.dto.ChangePasswordDTO;
import com.xuance.divination.dto.EmailCodeDTO;
import com.xuance.divination.dto.LoginDTO;
import com.xuance.divination.dto.RegisterDTO;
import com.xuance.divination.dto.ResetPasswordDTO;
import com.xuance.divination.dto.UpdateNicknameDTO;
import com.xuance.divination.dto.UserProfileDTO;
import com.xuance.divination.vo.UserVO;

public interface AuthService {
    Boolean sendRegisterEmailCode(EmailCodeDTO dto);
    Boolean sendResetPasswordEmailCode(EmailCodeDTO dto);
    UserVO register(RegisterDTO dto);
    UserVO login(LoginDTO dto);
    UserVO updateProfile(UserProfileDTO dto);
    UserVO updateNickname(UpdateNicknameDTO dto);
    Boolean changePassword(ChangePasswordDTO dto);
    Boolean resetPassword(ResetPasswordDTO dto);
}
