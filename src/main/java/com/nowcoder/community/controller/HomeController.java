package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 方法调用前，SpringMCV会自动实例化Model和Page，并且会将Page注入Model
        // 所以在Thymeleaf中可以直接访问Page中的数据，所以不用addAttribute
        page.setRows(discussPostService.findDiscussPostsRows(0));
        page.setPath("/index");
//        System.out.println("===========Page==============");
//        System.out.println(page);
//        System.out.println("===========Page info==============");
//        System.out.println("total:" + page.getTotal());
//        System.out.println("from:" + page.getFrom());
//        System.out.println("to:" + page.getTo());
//        System.out.println("offset:" + page.getOffset());
        List<DiscussPost> posts =  discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post : posts) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            System.out.println("post user id:" + post.getUserId());
            User user = userService.findUserById(post.getUserId());
            System.out.println("user:" + user);
            map.put("user", user);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }

}
