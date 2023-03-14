package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }

    @RequestMapping(path = "login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    @RequestMapping(path = "login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean remember,
                        Model model, HttpSession session, HttpServletResponse resp) {
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "Wrong Code");
            return "site/login";
        }
        int expiredSeconds = remember ? Constants.REMEMBER_EXPIRED_SECONDS : Constants.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            resp.addCookie(cookie);
            return "redirect:/index";
        }
        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        return "site/login";
    }

    @RequestMapping(path = "logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }


    @RequestMapping(path = "register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            System.out.println("register success:" + user);
            model.addAttribute("msg",
                    "register success, we've send a activation email to you, " +
                            "please activate your account asap");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }
        System.out.println("register failed:" + user);
        System.out.println("register failed map:" + map.toString());
        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        model.addAttribute("emailMsg", map.get("emailMsg"));
        System.out.println("Model info:" + model.toString());
        return "site/register";
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "activation{userId}/{code}", method = RequestMethod.GET)
    public String activateAccount(Model model,
                                  @PathVariable("userId") int userId,
                                  @PathVariable("code") String code) {
        int result = userService.activateAccount(userId, code);
        if (result == Constants.ACTIVATION_SUCCESS) {
            model.addAttribute("msg",
                    "Your account has been successfully activated.");
            model.addAttribute("target", "/login");
        } else if (result == Constants.ACTIVATION_REPEAT) {
            model.addAttribute("msg",
                    "Invalid operation, this account has already been activated.");
            model.addAttribute("target", "/login");
        } else {
            model.addAttribute("msg",
                    "Activate failed, your activation code is not correct");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse resp, HttpSession session) {
        // 1 gen
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 2 save in session
        session.setAttribute("kaptcha", text);

        // 3
        resp.setContentType("image/png");
        try {
            OutputStream os = resp.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("resp kaptcha error:" + e.getMessage());
        }
    }
}
