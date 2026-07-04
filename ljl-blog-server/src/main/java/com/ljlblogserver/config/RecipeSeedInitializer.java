package com.ljlblogserver.config;

import com.ljlblogserver.mapper.ArticleMapper;
import com.ljlblogserver.common.ArticleType;
import com.ljlblogserver.service.RecipeSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeSeedInitializer implements CommandLineRunner {

    private final ArticleMapper articleMapper;
    private final RecipeSeedService recipeSeedService;

    @Override
    public void run(String... args) throws Exception {
        if (articleMapper.count(ArticleType.RECIPE.getValue(), null, null, null) > 0) {
            return;
        }
        int count = recipeSeedService.importSeed(false);
        if (count > 0) {
            log.info("已导入 {} 条示例菜谱数据", count);
        }
    }
}
