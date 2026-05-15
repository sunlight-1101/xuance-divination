package com.xuance.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.EmailCodeDTO;
import com.xuance.divination.dto.LoginDTO;
import com.xuance.divination.dto.RegisterDTO;
import com.xuance.divination.dto.UserProfileDTO;
import com.xuance.divination.entity.User;
import com.xuance.divination.entity.VerificationCode;
import com.xuance.divination.mapper.UserMapper;
import com.xuance.divination.mapper.VerificationCodeMapper;
import com.xuance.divination.service.AuthService;
import com.xuance.divination.vo.UserVO;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String REGISTER_SCENE = "REGISTER";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final VerificationCodeMapper codeMapper;
    private final JavaMailSender mailSender;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AuthServiceImpl(UserMapper userMapper, VerificationCodeMapper codeMapper, JavaMailSender mailSender) {
        this.userMapper = userMapper;
        this.codeMapper = codeMapper;
        this.mailSender = mailSender;
    }

    @Override
    public Boolean sendRegisterEmailCode(EmailCodeDTO dto) {
        String email = normalizeEmail(dto == null ? null : dto.getEmail());
        validateEmail(email);
        if (!mailEnabled || !StringUtils.hasText(mailFrom)) {
            throw new BizException("邮箱验证码未启用，请先配置 SMTP 发信邮箱");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, email)
                .or()
                .eq(User::getEmail, email));
        if (count > 0) {
            throw new BizException("该邮箱已注册");
        }
        VerificationCode latest = codeMapper.selectOne(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getTarget, email)
                .eq(VerificationCode::getScene, REGISTER_SCENE)
                .orderByDesc(VerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreateTime() != null && latest.getCreateTime().plusSeconds(60).isAfter(LocalDateTime.now())) {
            throw new BizException("验证码发送太频繁，请稍后再试");
        }
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        VerificationCode record = new VerificationCode();
        record.setTarget(email);
        record.setScene(REGISTER_SCENE);
        record.setCode(code);
        record.setUsed(0);
        record.setExpireTime(LocalDateTime.now().plusMinutes(10));
        record.setCreateTime(LocalDateTime.now());
        codeMapper.insert(record);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("哲玄注册验证码");
        message.setText("你的注册验证码是：" + code + "\n\n验证码 10 分钟内有效。如非本人操作，请忽略。");
        mailSender.send(message);
        return true;
    }

    @Override
    public UserVO register(RegisterDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        if (!StringUtils.hasText(email)) {
            email = normalizeEmail(dto.getUsername());
        }
        validateEmail(email);
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BizException("密码不能为空");
        }
        verifyRegisterCode(email, dto.getEmailCode());
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, email)
                .or()
                .eq(User::getEmail, email));
        if (count > 0) {
            throw new BizException("该邮箱已注册");
        }
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(hash(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : email);
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO login(LoginDTO dto) {
        String account = normalizeEmail(dto.getUsername());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or()
                .eq(User::getEmail, account));
        if (user == null || !hash(dto.getPassword()).equals(user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已停用");
        }
        return toVO(user);
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

    private String hash(String password) {
        return DigestUtils.md5DigestAsHex(("xuance:" + password).getBytes());
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
        return vo;
    }

    private void verifyRegisterCode(String email, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("请先输入邮箱验证码");
        }
        VerificationCode record = codeMapper.selectOne(new LambdaQueryWrapper<VerificationCode>()
                .eq(VerificationCode::getTarget, email)
                .eq(VerificationCode::getScene, REGISTER_SCENE)
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

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BizException("请输入正确的邮箱地址");
        }
    }
}
