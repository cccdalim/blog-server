-- 兼容 MySQL 5.7+ / 8.0 的升级脚本

-- 1. articles 增加 article_type（若已存在会报错，可忽略）
ALTER TABLE articles ADD COLUMN article_type VARCHAR(20) NOT NULL DEFAULT 'blog' AFTER id;

-- 2. projects 表
CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    summary      VARCHAR(500),
    cover        VARCHAR(500),
    git_url      VARCHAR(500),
    demo_url     VARCHAR(500),
    featured     TINYINT(1)   NOT NULL DEFAULT 0,
    start_date   DATE         NOT NULL,
    end_date     DATE,
    tech_stack   JSON,
    dev_process  JSON,
    screenshots  JSON,
    timeline     JSON,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_articles_type ON articles (article_type);
CREATE INDEX idx_projects_start_date ON projects (start_date DESC);
CREATE INDEX idx_projects_featured ON projects (featured);

-- 3. slug 改为按 article_type 唯一（Blog 与 Docs 可同名 slug）
-- 若报错 Unknown key 'slug'，说明已是新结构，可跳过
ALTER TABLE articles DROP INDEX slug;
ALTER TABLE articles ADD UNIQUE KEY uk_article_type_slug (article_type, slug);
