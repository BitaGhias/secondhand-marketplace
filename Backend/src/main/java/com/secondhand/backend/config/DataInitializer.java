package com.secondhand.backend.config;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration class: "data initializer".
 * <p>
 * This class is part of the application configuration and is loaded by Spring at startup.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Component
public class DataInitializer implements CommandLineRunner { // بعد از اینکه برنامه کامل راه افتاد متد run() رو اجرا کن

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Runs.
     *
     * @param args the "args" value of type {@code String...}
     */
    @Override
    public void run(String... args) {
        createDefaultAdmin();
        createDefaultTestUser();
        createDefaultCities();
        createDefaultCategories();
    }

    /**
     * Creates default admin.
     */
    private void createDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("مدیر سیستم");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setBlocked(false);
            admin.setPhoneNumber("09123456789");
            admin.setEmail("admin@example.com");

            userRepository.save(admin);

            logger.info("ادمین پیش‌فرض ایجاد شد: username=admin");
        }
    }

    /**
     * Creates default test user.
     */
    private void createDefaultTestUser() {
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setFullName("کاربر تست");
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("123456"));
            testUser.setRole(Role.USER);
            testUser.setActive(true);
            testUser.setBlocked(false);
            testUser.setPhoneNumber("09123456788");
            testUser.setEmail("test@example.com");

            userRepository.save(testUser);

            logger.info("کاربر تست ایجاد شد: username=testuser");
            //توی terminal چاپ میشه تا بدونی چی اتفاق افتاده
        }
    }

    /**
     * Creates default cities.
     */
    private void createDefaultCities() {
        if (cityRepository.count() == 0) {
            List<String> cityNames = List.of(
                    "تهران", "مشهد", "اصفهان", "شیراز", "تبریز",
                    "کرج", "اهواز", "قم", "رشت", "کرمانشاه"
            );

            for (String name : cityNames) {
                City city = new City();
                city.setName(name);
                cityRepository.save(city);
            }

            logger.info("شهرهای پیش‌فرض ایجاد شدند.");
        }
    }

    /**
     * Creates default categories.
     */
    private void createDefaultCategories() {
        if (categoryRepository.count() == 0) {
            Category electronics = createCategory("الکترونیک", null);
            createCategory("موبایل", electronics);
            createCategory("لپ‌تاپ و کامپیوتر", electronics);

            Category vehicle = createCategory("وسایل نقلیه", null);
            createCategory("خودرو", vehicle);
            createCategory("موتورسیکلت", vehicle);

            Category home = createCategory("خانه و آشپزخانه", null);
            createCategory("لوازم خانگی", home);
            createCategory("مبلمان", home);

            createCategory("پوشاک", null);
            createCategory("ورزش و سرگرمی", null);
            createCategory("کتاب و لوازم تحریر", null);

            logger.info("دسته‌بندی‌های پیش‌فرض ایجاد شدند.");
        }
    }

    /**
     * Creates category.
     *
     * @param name the name
     * @param parent the "parent" value of type {@code Category}
     * @return the resulting {@code Category} instance
     */
    private Category createCategory(String name, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setParent(parent);
        return categoryRepository.save(category);
    }
}