package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TagMapper {

    @Select("SELECT id, name, slug FROM tags WHERE id = #{id}")
    Tag findById(@Param("id") Long id);

    @Select("SELECT id, name, slug FROM tags WHERE slug = #{slug}")
    Tag findBySlug(@Param("slug") String slug);

    @Select("SELECT id, name, slug FROM tags WHERE name = #{name}")
    Tag findByName(@Param("name") String name);

    @Select("SELECT id, name, slug FROM tags WHERE slug = #{slug} AND id != #{excludeId}")
    Tag findBySlugExclude(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    @Insert("INSERT INTO tags (name, slug) VALUES (#{name}, #{slug})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Select("""
            SELECT t.id, t.name, t.slug, COUNT(at.article_id) AS count
            FROM tags t
            LEFT JOIN article_tags at ON at.tag_id = t.id
            GROUP BY t.id, t.name, t.slug
            ORDER BY count DESC, t.name
            """)
    List<Tag> findAllWithCount();

    @Select("SELECT COUNT(*) FROM article_tags WHERE tag_id = #{tagId}")
    long countArticlesByTagId(@Param("tagId") Long tagId);

    @Delete("DELETE FROM article_tags WHERE tag_id = #{tagId}")
    int deleteArticleTagsByTagId(@Param("tagId") Long tagId);

    @Update("UPDATE tags SET name = #{name}, slug = #{slug} WHERE id = #{id}")
    int update(Tag tag);

    @Delete("DELETE FROM tags WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 删除没有任何文章引用的标签 */
    @Delete("""
            DELETE t FROM tags t
            LEFT JOIN article_tags at ON at.tag_id = t.id
            WHERE at.tag_id IS NULL
            """)
    int deleteOrphans();
}
