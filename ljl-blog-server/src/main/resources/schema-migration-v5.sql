-- 分类按 scope 分组：content（Blog/Docs）与 recipe（菜谱）

ALTER TABLE categories ADD COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'content' AFTER slug;

ALTER TABLE categories DROP INDEX slug;
ALTER TABLE categories ADD UNIQUE KEY uk_category_scope_slug (scope, slug);

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
