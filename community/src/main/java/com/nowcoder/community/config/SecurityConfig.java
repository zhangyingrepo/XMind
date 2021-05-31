package com.nowcoder.community.config;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // AuthenticationManager: 认证的核心接口
        // AuthenticationManagerBuilder: 用于构建AuthenticationManager对象的工具, ProviderManager是AuthenticationManager接口的默认实现
        // AuthenticationProvider: AuthenticationManager持有一组AuthenticationProvider，每个AuthenticationProvider负责一种认证(密码，指纹)

        // 内置的认证规则
        // auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("12345"));

        // 自定义认证规则
        auth.authenticationProvider(new AuthenticationProvider() {
            /**
             * 用于封装认证信息的接口,不同的实现类代表不同类型的认证信息
             * @param authentication
             * @return
             * @throws AuthenticationException
             */
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = (String) authentication.getCredentials();
                User user = userService.findUserByName(username);
                if (user == null) {
                    throw new UsernameNotFoundException("账号不存在!");
                }
                password = CommunityUtil.md5(password + user.getSalt());
                if (!user.getPassword().equals(password)) {
                    throw new BadCredentialsException("密码不正确!");
                }
                return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            }

            /**
             * 支持的认证类型
             * @param aClass
             * @return
             */
            @Override
            public boolean supports(Class<?> aClass) {
                return UsernamePasswordAuthenticationToken.class.equals(aClass);
            }
        });
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 登录相关配置
        http.formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    httpServletResponse.sendRedirect("");
                })
                .failureHandler((httpServletRequest, httpServletResponse, authentication) -> {});
    }
}
