package com.zhexuan.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.common.JwtUtil;
import com.zhexuan.divination.common.PageResult;
import com.zhexuan.divination.dto.ChangePasswordDTO;
import com.zhexuan.divination.dto.EmailCodeDTO;
import com.zhexuan.divination.dto.LoginDTO;
import com.zhexuan.divination.dto.RegisterDTO;
import com.zhexuan.divination.dto.ResetPasswordDTO;
import com.zhexuan.divination.dto.UpdateNicknameDTO;
import com.zhexuan.divination.dto.UserProfileDTO;
import com.zhexuan.divination.entity.User;
import com.zhexuan.divination.entity.VerificationCode;
import com.zhexuan.divination.mapper.UserMapper;
import com.zhexuan.divination.mapper.VerificationCodeMapper;
import com.zhexuan.divination.service.AuthService;
import com.zhexuan.divination.vo.UserVO;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String REGISTER_SCENE = "REGISTER";
    private static final String RESET_PASSWORD_SCENE = "RESET_PASSWORD";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final VerificationCodeMapper codeMapper;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AuthServiceImpl(UserMapper userMapper, VerificationCodeMapper codeMapper, JavaMailSender mailSender, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.codeMapper = codeMapper;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Boolean sendRegisterEmailCode(EmailCodeDTO dto) {
        String email = normalizeEmail(dto == null ? null : dto.getEmail());
        validateEmail(email);
        ensureMailEnabled();
        if (findByEmail(email) != null) {
            throw new BizException("该邮箱已注册");
        }
        assertCodeSendInterval(email, REGISTER_SCENE);
        String code = createCode(email, REGISTER_SCENE);
        sendMail(email, "哲玄注册验证码", "你的注册验证码是：" + code + "\n\n验证码 10 分钟内有效。如非本人操作，请忽略。");
        return true;
    }

    @Override
    public Boolean sendResetPasswordEmailCode(EmailCodeDTO dto) {
        String email = normalizeEmail(dto == null ? null : dto.getEmail());
        validateEmail(email);
        ensureMailEnabled();
        if (findByEmail(email) == null) {
            throw new BizException("该邮箱尚未注册");
        }
        assertCodeSendInterval(email, RESET_PASSWORD_SCENE);
        String code = createCode(email, RESET_PASSWORD_SCENE);
        sendMail(email, "哲玄重置密码验证码", "你的重置密码验证码是：" + code + "\n\n验证码 10 分钟内有效。如非本人操作，请忽略。");
        return true;
    }

    @Override
    public UserVO register(RegisterDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        if (!StringUtils.hasText(email)) {
            email = normalizeEmail(dto.getUsername());
        }
        validateEmail(email);
        validatePassword(dto.getPassword());
        verifyCode(email, dto.getEmailCode(), REGISTER_SCENE);
        if (findByEmail(email) != null) {
            throw new BizException("该邮箱已注册");
        }
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(hash(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : email);
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return toVOWithToken(user);
    }

    @Override
    public UserVO login(LoginDTO dto) {
        String account = normalizeEmail(dto.getUsername());
        User user = findByEmail(account);
        if (user == null || !hash(dto.getPassword()).equals(user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已停用");
        }
        return toVOWithToken(user);
    }

    @Override
    public UserVO updateProfile(UserProfileDTO dto) {
        if (dto.getUserId() == null) {
            throw new BizException("游客模式不能保存个人档案");
        }
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setGender(dto.getGender());
        user.setBirthDate(dto.getBirthDate());
        user.setBirthTime(dto.getBirthTime());
        user.setBirthPlace(dto.getBirthPlace());
        user.setBirthDayGanZhi(dto.getBirthDayGanZhi());
        user.setBirthDayMaster(dto.getBirthDayMaster());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public UserVO updateNickname(UpdateNicknameDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            throw new BizException("请先登录后再修改昵称");
        }
        String nickname = dto.getNickname() == null ? "" : dto.getNickname().trim();
        if (!StringUtils.hasText(nickname)) {
            throw new BizException("昵称不能为空");
        }
        if (nickname.length() > 30) {
            throw new BizException("昵称不能超过 30 个字");
        }
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setNickname(nickname);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public Boolean changePassword(ChangePasswordDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            throw new BizException("请先登录后再修改密码");
        }
        if (!StringUtils.hasText(dto.getOldPassword())) {
            throw new BizException("请输入原密码");
        }
        validatePassword(dto.getNewPassword());
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!hash(dto.getOldPassword()).equals(user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(hash(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return true;
    }

    @Override
    public Boolean resetPassword(ResetPasswordDTO dto) {
        String email = normalizeEmail(dto == null ? null : dto.getEmail());
        validateEmail(email);
        validatePassword(dto.getNewPassword());
        verifyCode(email, dto.getEmailCode(), RESET_PASSWORD_SCENE);
        User user = findByEmail(email);
        if (user == null) {
            throw new BizException("该邮箱尚未注册");
        }
        user.setPassword(hash(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return true;
    }

    @Override
    public PageResult<UserVO> listAllUsers(Long adminUserId, String keyword, long page, long size) {
        if (adminUserId == null) {
            throw new BizException("请先登录");
        }
        User admin = userMapper.selectById(adminUserId);
        if (admin == null) {
            throw new BizException("用户不存在");
        }
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BizException("无权限访问");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> list = userPage.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList());
        return PageResult.of(list, userPage.getTotal(), page, size);
    }

    private String hash(String password) {
        return DigestUtils.md5DigestAsHex(("xuance:" + password).getBytes(StandardCharsets.UTF_8));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate());
        vo.setBirthTime(user.getBirthTime());
        vo.setBirthPlace(user.getBirthPlace());
        vo.setBirthDayGanZhi(user.getBirthDayGanZhi());
        vo.setBirthDayMaster(user.getBirthDayMaster());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return vo;
    }

    private UserVO toVOWithToken(User user) {
        UserVO vo = toVO(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        vo.setToken(token);
        return vo;
    }

    private void verifyCode(String email, String code, String scene) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("请先输入邮箱验证码");
        }
        VerificationCode record = codeMapper.selectOne(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getTarget, email)
                .eq(VerificationCode::getScene, scene)
                .eq(VerificationCode::getUsed, 0)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (record == null || record.getExpireTime() == null || record.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!record.getCode().equals(String.valueOf(code).trim())) {
            throw new BizException("验证码不正确");
        }
        record.setUsed(1);
        codeMapper.updateById(record);
    }

    private void ensureMailEnabled() {
        if (!mailEnabled || !StringUtils.hasText(mailFrom)) {
            throw new BizException("邮箱验证码未启用，请先配置 SMTP 发信邮箱");
        }
    }

    private void assertCodeSendInterval(String email, String scene) {
        VerificationCode latest = codeMapper.selectOne(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getTarget, email)
                .eq(VerificationCode::getScene, scene)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreateTime() != null && latest.getCreateTime().plusSeconds(60).isAfter(LocalDateTime.now())) {
            throw new BizException("验证码发送太频繁，请稍后再试");
        }
    }

    private String createCode(String email, String scene) {
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        VerificationCode record = new VerificationCode();
        record.setTarget(email);
        record.setScene(scene);
        record.setCode(code);
        record.setUsed(0);
        record.setExpireTime(LocalDateTime.now().plusMinutes(10));
        record.setCreateTime(LocalDateTime.now());
        codeMapper.insert(record);
        return code;
    }

    private void sendMail(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, email)
                .or()
                .eq(User::getEmail, email)
                .last("LIMIT 1"));
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BizException("密码至少需要 6 位");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BizException("请输入正确的邮箱地址");
        }
    }
}
