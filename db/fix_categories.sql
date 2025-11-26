-- fix_categories.sql
-- 用于修复缺失 categories 表和 books.category_id 无效引用的问题
-- 在执行前请备份数据：
-- mysqldump -u root -p book_db books categories > books_backup.sql

SET FOREIGN_KEY_CHECKS = 0;

-- 1) 如果 categories 表不存在，创建之
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 插入默认分类（'未分类'），若已存在则忽略
INSERT IGNORE INTO categories (name) VALUES ('未分类');

-- 获取默认分类 id（假设 name 唯一）
-- SELECT id FROM categories WHERE name = '未分类';

-- 3) 尝试删除指向 categories 的外键约束（如果存在），以便修改列为可空
-- NOTE: 下面查询和 DROP 语句仅在有权限的情况下有效；请根据实际 FK 名称调整
-- 先查出约束名（在 MySQL 中）

-- 下面的查询会列出所有引用 categories 的外键
SELECT CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'books'
  AND REFERENCED_TABLE_NAME = 'categories';

-- 手动检查上面的 constraint_name，然后运行类似：
-- ALTER TABLE books DROP FOREIGN KEY FKleqa3hhc0uhfvurq6mil47xk0;

-- 4) 将 books.category_id 修改为可空（前提是没有外键或外键已移除）
ALTER TABLE books MODIFY category_id BIGINT NULL;

-- 5) 修正无效 category_id 值（例如0）为 NULL
UPDATE books SET category_id = NULL WHERE category_id = 0;

-- 6) （可选）如果你想把 NULL 的书籍归入默认分类，请先查看默认分类 ID
-- 假设默认分类 id 为 1（请替换为实际 id）: updateBooksToDefaultCategoryId
-- UPDATE books SET category_id = 1 WHERE category_id IS NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- END
