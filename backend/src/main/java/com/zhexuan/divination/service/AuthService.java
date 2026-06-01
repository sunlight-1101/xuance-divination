package com.zhexuan.divination.service;

import com.zhexuan.divination.dto.ChangePasswordDTO;
import com.zhexuan.divination.dto.EmailCodeDTO;
import com.zhexuan.divination.dto.LoginDTO;
import com.zhexuan.divination.dto.RegisterDTO;
import com.zhexuan.divination.dto.ResetPasswordDTO;
import com.zhexuan.divination.dto.UpdateNicknameDTO;
import com.zhexuan.divination.dto.UserProfileDTO;
import com.zhexuan.divination.common.PageResult;
import com.zhexuan.divination.vo.UserVO;

public interface AuthService {
    Boolean sendRegisterEmailCode(EmailCodeDTO dto);
    Boolean sendResetPasswordEmailCode(EmailCodeDTO dto);
    UserVO register(RegisterDTO dto);
    UserVO login(LoginDTO dto);
    UserVO updateProfile(UserProfileDTO dto);
    UserVO updateNickname(UpdateNicknameDTO dto);
    Boolean changePassword(ChangePasswordDTO dto);
    Boolean resetPassword(ResetPasswordDTO dto);
    PageResult<UserVO> listAllUsers(Long adminUserId, String keyword, long page, long size);
}
