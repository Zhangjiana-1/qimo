# 数据库修复脚本与说明

本目录包含用于修复 `categories` 表缺失或 `books` 表中 `category_id` 无效引用的 SQL 脚本。

## 为什么需要这些脚本
- 该项目中 `Book` 实体有 `ManyToOne` 指向 `Category`（`books.category_id`）。如果数据库缺少 `categories` 表或 `books.category_id` 指向了不存在的分类（例如 `category_id=0`），则在查询、启动或渲染页面（如 /books、/admin/books）时可能引发异常并返回 500 错误。

## 使用说明（强烈建议备份数据库）
1. 备份 books 和 categories 表（务必先备份）
   ```bash
   mysqldump -u <user> -p book_db books categories > books_categories_backup.sql
   ```

2. 使用 MySQL 客户端或者 Workbench 执行：
   - 创建 `categories` 表（如果不存在）
   - 插入默认分类（名字：`未分类`）
   - 删除 `books` 表中指向 `categories` 的外键（如果存在并阻止修改）
   - 将 `books.category_id` 修改为可空
   - 将 `category_id=0` 的行改为 NULL 或设置默认分类 id（可选）

   可以直接运行 `db/fix_categories.sql`（建议登录 MySQL 客户端后逐条执行以便检查）：
   ```sql
   SOURCE db/fix_categories.sql;
   ```

3. 修复完成后，重启应用（如果你希望 Hibernate 自动处理结构更新，可以将 `application.yml` 中 `ddl-auto` 恢复为 `update`）

## 注意
- 如果你的 MySQL 用户权限不足以删除外键或修改列，脚本中某些 DDL 语句会失败。在这种情况下，请联系你的 DB 管理员或手动在 DB 管理工具中操作。

---

如果你希望我继续在 `DataInitRunner` 中添加更多的自动化修复逻辑（例如强制删除外键，或在 DDL 执行失败时以更安全的方式记录并继续应用启动），我可以继续完善。