-- 标签按 scope 分组：content（Blog/Docs）与 recipe（菜谱）

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
