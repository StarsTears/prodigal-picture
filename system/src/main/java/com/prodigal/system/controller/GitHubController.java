package com.prodigal.system.controller;

import com.prodigal.system.api.githelper.GitHubService;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.model.entity.GitHubCommitInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-picture
 * @date 2025/4/16 10:29
 * @description: GitHub-REST控制器
 */
@RestController
@RequestMapping("/git")
public class GitHubController {
    @Resource
    private GitHubService gitHubService;
    @GetMapping("/commits")
    public BaseResult<List<GitHubCommitInfo>> getCommits() {
        List<GitHubCommitInfo> commits = gitHubService.getRepositoryCommits();
        return BaseResult.success().data(commits);
    }
}
