package com.demo.controller;

import com.demo.core.JwtUtil;
import com.demo.entity.User;
import com.demo.service.HelloService;
import com.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ying.zhang01
 */
@RestController
public class JwtController {
    private static final Logger log = LoggerFactory.getLogger(JwtController.class);

    private HelloService helloService;
    private UserService userService;

    public JwtController(HelloService helloService, UserService userService) {
        this.helloService = helloService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser != null) {
            log.debug("Login User: [{}]", loginUser);
            int userId = loginUser.getUserId();
            return JwtUtil.generate(String.valueOf(userId));
        }
        return "JWT登录失败";
    }
}
