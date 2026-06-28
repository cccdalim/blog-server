package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {

    Article findBySlug(@Param("slug") String slug, @Param("articleType") String articleType);

    List<Article> findPage(@Param("articleType") String articleType,
                           @Param("categorySlug") String categorySlug,
                           @Param("tag") String tag,
                           @Param("keyword") String keyword,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    long count(@Param("articleType") String articleType,
               @Param("categorySlug") String categorySlug,
               @Param("tag") String tag,
               @Param("keyword") String keyword);

    List<Article> findLatest(@Param("articleType") String articleType, @Param("limit") int limit);

    List<Article> findAllSorted(@Param("articleType") String articleType);

    /** 资源清理：扫描所有类型文章 */
    List<Article> findAllForAssetScan();

    int insert(Article article);

    int update(Article article);

    int deleteBySlug(@Param("slug") String slug, @Param("articleType") String articleType);

    int deleteTagsByArticleId(@Param("articleId") Long articleId);

    int insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    List<String> findTagsByArticleId(@Param("articleId") Long articleId);
}
