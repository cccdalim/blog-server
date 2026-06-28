package com.ljlblogserver.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public final class AboutDto {

    private AboutDto() {
    }

    @Data
    public static class ProfileDto {
        private String name;
        private String avatar;
        private String title;
        private String bio;
        private String tagline;
    }

    @Data
    public static class SocialLinksDto {
        private String github;
        private String email;
        private String twitter;
    }

    @Data
    public static class AboutDataDto {
        private ProfileDto profile;
        private SocialLinksDto social;
    }

    @Data
    public static class TimelineItemDto {
        private String date;
        private String title;
        private String description;
    }

    @Data
    public static class LearningPathItemDto {
        private String id;
        private String title;
        private String description;
        private String status;
        private List<String> skills = new ArrayList<>();
    }

    @Data
    public static class AboutPageDto {
        private ProfileDto profile;
        private SocialLinksDto social;
        private List<TimelineItemDto> timeline = new ArrayList<>();
        private List<LearningPathItemDto> learningPath = new ArrayList<>();
    }

    @Data
    public static class SkillDto {
        private String name;
        private String level;
        private String category;
    }

    @Data
    public static class SkillsFileDto {
        private List<SkillDto> skills = new ArrayList<>();
    }
}
