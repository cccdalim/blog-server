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

    @Select("SELECT id, name, slug, scope FROM tags WHERE id = #{id}")
    Tag findById(@Param("id") Long id);

    @Select("SELECT id, name, slug, scope FROM tags WHERE slug = #{slug} AND scope = #{scope}")
    Tag findBySlugAndScope(@Param("slug") String slug, @Param("scope") String scope);

    @Select("SELECT id, name, slug, scope FROM tags WHERE name = #{name} AND scope = #{scope}")
    Tag findByNameAndScope(@Param("name") String name, @Param("scope") String scope);

    @Select("SELECT id, name, slug, scope FROM tags WHERE slug = #{slug} AND scope = #{scope} AND id != #{excludeId}")
    Tag findBySlugExclude(@Param("slug") String slug, @Param("scope") String scope, @Param("excludeId") Long excludeId);

    @Insert("INSERT INTO tags (name, slug, scope) VALUES (#{name}, #{slug}, #{scope})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Select("""
            SELECT t.id, t.name, t.slug, t.scope, COUNT(at.article_id) AS count
            FROM tags t
            LEFT JOIN article_tags at ON at.tag_id = t.id
            WHERE t.scope = #{scope}
            GROUP BY t.id, t.name, t.slug, t.scope
            ORDER BY count DESC, t.name
            """)
    List<Tag> findAllWithCountByScope(@Param("scope") String scope);

    @Select("SELECT COUNT(*) FROM article_tags WHERE tag_id = #{tagId}")
    long countArticlesByTagId(@Param("tagId") Long tagId);

    @Delete("DELETE FROM article_tags WHERE tag_id = #{tagId}")
    int deleteArticleTagsByTagId(@Param("tagId") Long tagId);

    @Update("UPDATE tags SET name = #{name}, slug = #{slug} WHERE id = #{id}")
    int update(Tag tag);

    @Delete("DELETE FROM tags WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM tags")
    long countAll();

    /** 删除没有任何文章引用的标签 */
    @Delete("""
            DELETE t FROM tags t
            LEFT JOIN article_tags at ON at.tag_id = t.id
            WHERE at.tag_id IS NULL
            """)
    int deleteOrphans();
}
