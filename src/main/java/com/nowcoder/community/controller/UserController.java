package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model) {
        if (headerImg == null) {
            model.addAttribute("error", "no image selected!");
            return "site/setting";
        }

        String filename = headerImg.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf('.'));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "image format not correct!");
            return "site/setting";
        }

        filename = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("upload file failed: " + e.getMessage());
            throw new RuntimeException("upload file failed", e);
        }

        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeaderURL(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse resp) {
        filename = uploadPath + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf('.'));
        resp.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filename);
             OutputStream os = resp.getOutputStream()) {
            byte[] buff = new byte[1024];
            int index = 0;
            while ((index = fis.read(buff)) != -1) {
                os.write(buff, 0, index);
            }
        } catch (IOException e) {
            logger.error("read header failed: " + e.getMessage());
        }
    }


    @LoginRequired
    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public String changePassword(String originalPassword, String newPassword, String confirmPassword, Model model) {
        if (StringUtils.isBlank(originalPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confirmPassword)) {
            model.addAttribute("error", "password can not be empty!");
            return "site/setting";
        }
        User user = hostHolder.getUser();
        if (userService.updatePassword(user, originalPassword, newPassword) == 0) {
            model.addAttribute("originalError", "wrong original password!");
        } else {
            model.addAttribute("success", "change password success!");
        }
        return "site/setting";
    }
}
