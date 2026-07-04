-- =============================================================================
-- 菜谱模块升级脚本（合并 v3 + v4 + v5）
-- =============================================================================
-- 适用：已执行 schema-migration-v2.sql 的现有数据库，一次性升级到菜谱模块完整结构。
--
-- 包含内容：
--   v3  补充菜系分类（家常菜、川菜、粤菜…）
--   v4  标签 scope 分组（content / recipe）
--   v5  分类 scope 分组（content / recipe）
--
-- 使用方式（MySQL）：
--   USE ljl_blog;
--   SOURCE /path/to/schema-migration-v3-to-v5.sql;
--
-- 注意：
--   - 整份脚本只需执行一次；重复执行时 ALTER / ADD COLUMN 可能报错，可忽略已完成的步骤。
--   - 执行完成后请重启后端服务。
-- =============================================================================

USE ljl_blog;


-- -----------------------------------------------------------------------------
-- v4 · 标签 scope 分组
-- -----------------------------------------------------------------------------

ALTER TABLE tags ADD COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'content' AFTER slug;

-- 若旧库 slug 为全局唯一，需先删除再建复合唯一键（已存在 uk_tag_scope_slug 则跳过）
ALTER TABLE tags DROP INDEX slug;
ALTER TABLE tags ADD UNIQUE KEY uk_tag_scope_slug (scope, slug);

-- 将仅被菜谱引用的标签归入 recipe 分组
UPDATE tags t
SET t.scope = 'recipe'
WHERE EXISTS (
    SELECT 1 FROM article_tags at
    INNER JOIN articles a ON a.id = at.article_id AND a.article_type = 'recipe'
    WHERE at.tag_id = t.id
)
AND NOT EXISTS (
    SELECT 1 FROM article_tags at2
    INNER JOIN articles a2 ON a2.id = at2.article_id AND a2.article_type IN ('blog', 'doc')
    WHERE at2.tag_id = t.id
);


-- -----------------------------------------------------------------------------
-- v5 · 分类 scope 分组（结构变更）
-- -----------------------------------------------------------------------------

ALTER TABLE categories ADD COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'content' AFTER slug;

ALTER TABLE categories DROP INDEX slug;
ALTER TABLE categories ADD UNIQUE KEY uk_category_scope_slug (scope, slug);


-- -----------------------------------------------------------------------------
-- v3 · 补充菜系分类（含 scope，与 v5 结构一致）
-- -----------------------------------------------------------------------------

INSERT INTO categories (name, slug, scope) VALUES
    ('家常菜', 'home-cooking', 'recipe'),
    ('川菜', 'sichuan', 'recipe'),
    ('粤菜', 'cantonese', 'recipe'),
    ('烘焙', 'baking', 'recipe'),
    ('汤粥', 'soup-porridge', 'recipe'),
    ('小吃', 'snacks', 'recipe')
ON DUPLICATE KEY UPDATE name = VALUES(name), scope = VALUES(scope);


-- -----------------------------------------------------------------------------
-- v5 · 分类 scope 数据修正（兼容 v3 早期无 scope 的插入记录）
-- -----------------------------------------------------------------------------

UPDATE categories
SET scope = 'recipe'
WHERE slug IN ('home-cooking', 'sichuan', 'cantonese', 'baking', 'soup-porridge', 'snacks');

UPDATE categories c
SET c.scope = 'recipe'
WHERE EXISTS (
    SELECT 1 FROM articles a
    WHERE a.category_id = c.id AND a.article_type = 'recipe'
)
AND NOT EXISTS (
    SELECT 1 FROM articles a2
    WHERE a2.category_id = c.id AND a2.article_type IN ('blog', 'doc')
);
