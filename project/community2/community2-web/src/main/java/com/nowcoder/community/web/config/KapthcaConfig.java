package com.nowcoder.community.web.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * @author zhangying
 */
@Configuration
@EnableConfigurationProperties(KaptchaProperties.class)
public class KapthcaConfig {
    @Autowired
    private KaptchaProperties kaptchaProperties;

    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", kaptchaProperties.getHeight());
        properties.setProperty("kaptcha.image.font.size", kaptchaProperties.getSize());
        properties.setProperty("kaptcha.image.font.color", kaptchaProperties.getColor());
        properties.setProperty("kaptcha.textproducer.char.string", kaptchaProperties.getString());
        properties.setProperty("kaptcha.textproducer.char.length", kaptchaProperties.getLength());
        properties.setProperty("kaptcha.noise.impl", kaptchaProperties.getNoise());

        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
