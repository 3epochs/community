package com.nowcoder.community.service;


import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.Constants;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 1 check params
        if (user == null) {
            throw new IllegalArgumentException("Argument can not be null!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "username can not be empty!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password can not be empty!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "email can not be empty!");
            return map;
        }
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "username already exist!");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "this email has been registered!");
            return map;
        }

        // 2 register
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword((CommunityUtil.md5(user.getPassword() + user.getSalt())));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 3 send activate email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "activate your account", content);

        return map;
    }

    public int activateAccount(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("No such user match this userId!");
        }
        if (user.getStatus() == 1) {
            return Constants.ACTIVATION_REPEAT;
        }
        if (!user.getActivationCode().equals(code)) {
            return Constants.ACTIVATION_FAILURE;
        }
        userMapper.updateStatus(userId, 1);
        return Constants.ACTIVATION_SUCCESS;
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "username can not be empty!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "password can not be empty!");
            return map;
        }

        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "username do not exist!");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "this account is not activated!");
            return map;
        }

        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "wrong password");
        }

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        long expiredMills = (long) expiredSeconds * 1000;
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredMills));

        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateLoginTicketStatus(ticket, 1);
    }

    public LoginTicket getLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }
}
