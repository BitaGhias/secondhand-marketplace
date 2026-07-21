package com.secondhand.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * سرو کردن فایل‌های آپلود شده (تصاویر آگهی‌ها) به صورت استاتیک
 * مثال: http://localhost:8080/uploads/171234567890.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
/*به Spring بگو تصاویر آپلودشده رو که
 روی هارد ذخیره شدن،
  از طریق URL قابل دسترس کن تا فرانت بتونه نشونشون بده*/