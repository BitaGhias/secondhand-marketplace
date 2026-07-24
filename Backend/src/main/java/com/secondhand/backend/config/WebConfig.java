package com.secondhand.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class: "web config".
 * <p>
 * This class is part of the application configuration and is loaded by Spring at startup.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Adds resource handlers.
     *
     * @param registry the "registry" value of type {@code ResourceHandlerRegistry}
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
/*به Spring بگو تصاویر آپلودشده رو که
 روی هارد ذخیره شدن،
  از طریق URL قابل دسترس کن تا فرانت بتونه نشونشون بده*/