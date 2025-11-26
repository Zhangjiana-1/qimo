package com.example.qimo.runner;

import com.example.qimo.entity.Category;
import com.example.qimo.entity.User;
import com.example.qimo.repository.CategoryRepository;
import com.example.qimo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 创建管理员账号（兼容旧逻辑）
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEmail("admin@example.com");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("管理员账户已创建：username=admin, password=Admin@123");
        }

        // 2. 确保 categories 表存在并包含一个默认分类（避免因缺少分类表导致页面 500）
        try {
            long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class);
            // 表存在，继续
        } catch (Exception ex) {
            // categories 表不存在，尝试在数据库中创建一个最小化的 categories 表
            System.out.println("检测到 categories 表不存在，正在尝试创建...");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS categories (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        }

        // 插入默认分类（如果不存在）
        try {
            if (!categoryRepository.existsByName("未分类")) {
                Category defaultCategory = new Category();
                defaultCategory.setName("未分类");
                categoryRepository.save(defaultCategory);
                System.out.println("已创建默认分类：未分类");
            }
        } catch (Exception e) {
            System.out.println("插入默认分类时发生异常（请确认权限）：" + e.getMessage());
        }

        // 3. 修复 books 表中 category_id = 0 的无效引用（更新为 NULL） - 使用 JDBC 以避免 JPA 触发异常
        try {
            // 确保 books.category_id 允许为 NULL
            try {
                // 1) 如果存在外键约束引用 categories，先删除这些约束（避免修改列类型/允许 NULL 失败）
                try {
                    var fkNames = jdbcTemplate.queryForList(
                        "SELECT constraint_name FROM information_schema.KEY_COLUMN_USAGE WHERE table_schema = DATABASE() AND table_name = 'books' AND referenced_table_name = 'categories';",
                        String.class);
                    for (String fk : fkNames) {
                        try {
                            jdbcTemplate.execute("ALTER TABLE books DROP FOREIGN KEY " + fk);
                        } catch (Exception ignore) {
                            // 忽略删除失败
                        }
                    }
                } catch (Exception ignore) {
                    // 忽略查询失败
                }

                // 2) 再尝试把 books.category_id 设置为可空
                jdbcTemplate.execute("ALTER TABLE books MODIFY category_id BIGINT NULL;");
            } catch (Exception e) {
                // 忽略修改列属性时的异常（在某些数据库/权限下失败），但我们仍尝试更新数据
            }

            int updated = jdbcTemplate.update("UPDATE books SET category_id = NULL WHERE category_id = 0");
            if (updated > 0) {
                System.out.println("已将 " + updated + " 条记录的 category_id 从 0 更新为 NULL，避免外键引用异常。");
            }
        } catch (Exception ex) {
            // 在某些环境中，books 表也可能不存在或 SQL 权限不足；捕获并打印以便后续处理
            System.out.println("尝试清理 books 表中的无效 category_id 时发生异常：" + ex.getMessage());
        }
    }
}