package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name, slug FROM categories WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    @Select("SELECT id, name, slug FROM categories WHERE slug = #{slug}")
    Category findBySlug(@Param("slug") String slug);

    @Select("SELECT id, name, slug FROM categories WHERE slug = #{slug} AND id != #{excludeId}")
    Category findBySlugExclude(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    @Select("""
            SELECT c.id, c.name, c.slug, COUNT(a.id) AS count
            FROM categories c
            LEFT JOIN articles a ON a.category_id = c.id
            GROUP BY c.id, c.name, c.slug
            ORDER BY count DESC, c.name
            """)
    List<Category> findAllWithCount();

    @Select("SELECT COUNT(*) FROM articles WHERE category_id = #{categoryId}")
    long countArticlesByCategoryId(@Param("categoryId") Long categoryId);

    @Insert("INSERT INTO categories (name, slug) VALUES (#{name}, #{slug})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE categories SET name = #{name}, slug = #{slug} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM categories WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
