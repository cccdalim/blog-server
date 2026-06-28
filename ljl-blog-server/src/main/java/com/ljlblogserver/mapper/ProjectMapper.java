package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {

    List<Project> findPage(@Param("keyword") String keyword,
                           @Param("featured") Boolean featured,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    long count(@Param("keyword") String keyword, @Param("featured") Boolean featured);

    List<Project> findFeatured(@Param("limit") int limit);

    List<Project> findAllSorted();

    Project findById(@Param("id") Long id);

    long countAll();

    int insert(Project project);

    int update(Project project);

    int deleteById(@Param("id") Long id);

    int deleteAll();
}
