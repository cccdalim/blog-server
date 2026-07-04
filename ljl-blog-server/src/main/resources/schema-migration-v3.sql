-- 菜谱模块：补充菜系分类（已有库执行一次即可，重复执行无害）

INSERT INTO categories (name, slug) VALUES
    ('家常菜', 'home-cooking'),
    ('川菜', 'sichuan'),
    ('粤菜', 'cantonese'),
    ('烘焙', 'baking'),
    ('汤粥', 'soup-porridge'),
    ('小吃', 'snacks')
ON DUPLICATE KEY UPDATE name = VALUES(name);
