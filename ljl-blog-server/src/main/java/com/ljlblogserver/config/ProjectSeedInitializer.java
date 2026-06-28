package com.ljlblogserver.config;

import com.ljlblogserver.service.ProjectSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSeedInitializer implements CommandLineRunner {

    private final ProjectSeedService projectSeedService;

    @Override
    public void run(String... args) throws Exception {
        int count = projectSeedService.importSeed(false);
        if (count > 0) {
            log.info("已导入 {} 条示例项目数据", count);
        }
    }
}
