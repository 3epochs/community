package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }

    @RequestMapping(path = "login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
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
}
