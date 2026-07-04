CREATE DATABASE IF NOT EXISTS ljl_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ljl_blog;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(100) NOT NULL,
    slug  VARCHAR(100) NOT NULL,
    scope VARCHAR(20)  NOT NULL DEFAULT 'content',
    UNIQUE KEY uk_category_scope_slug (scope, slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(100) NOT NULL,
    slug  VARCHAR(100) NOT NULL,
    scope VARCHAR(20)  NOT NULL DEFAULT 'content',
    UNIQUE KEY uk_tag_scope_slug (scope, slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS articles (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_type VARCHAR(20)  NOT NULL DEFAULT 'blog',
    slug         VARCHAR(200) NOT NULL,
    title        VARCHAR(500) NOT NULL,
    summary      TEXT,
    cover_path   VARCHAR(500),
    content_path VARCHAR(500) NOT NULL,
    category_id  BIGINT,
    publish_date DATE         NOT NULL,
    read_time    INT,
    featured     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS article_tags (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    CONSTRAINT fk_at_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    CONSTRAINT fk_at_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS photos (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_path  VARCHAR(500) NOT NULL,
    title      VARCHAR(200) NOT NULL,
    city       VARCHAR(100),
    country    VARCHAR(100),
    category   VARCHAR(100),
    photo_date DATE         NOT NULL,
    width      INT,
    height     INT,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_articles_publish_date ON articles (publish_date DESC);
CREATE INDEX idx_articles_category ON articles (category_id);
CREATE INDEX idx_articles_type ON articles (article_type);
CREATE UNIQUE INDEX uk_article_type_slug ON articles (article_type, slug);
CREATE INDEX idx_photos_date ON photos (photo_date DESC);

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

CREATE INDEX idx_projects_start_date ON projects (start_date DESC);
CREATE INDEX idx_projects_featured ON projects (featured);

INSERT INTO categories (name, slug, scope) VALUES
    ('前端框架', 'frontend-framework', 'content'),
    ('后端开发', 'backend', 'content'),
    ('数据库', 'database', 'content'),
    ('DevOps', 'devops', 'content'),
    ('编程语言', 'programming-language', 'content'),
    ('工程化', 'engineering', 'content'),
    ('家常菜', 'home-cooking', 'recipe'),
    ('川菜', 'sichuan', 'recipe'),
    ('粤菜', 'cantonese', 'recipe'),
    ('烘焙', 'baking', 'recipe'),
    ('汤粥', 'soup-porridge', 'recipe'),
    ('小吃', 'snacks', 'recipe')
ON DUPLICATE KEY UPDATE name = VALUES(name), scope = VALUES(scope);
