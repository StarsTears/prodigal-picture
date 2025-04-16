package com.prodigal.system.api.githelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodigal.system.model.entity.GitHubCommitInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-picture
 * @date 2025/4/16 9:47
 * @description: GitHub API服务类
 */
@Service
public class GitHubService {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private final RestTemplate restTemplate;
    @Value("${github.owner}")
    private String owner;
    @Value("${github.repo}")
    private String repo;

    public GitHubService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * 获取仓库的提交记录：https://api.github.com/repos/StarsTears/prodigal-picture/commits
     * @return
     */
    public List<GitHubCommitInfo> getRepositoryCommits() {
        String url = String.format("%s/repos/%s/%s/commits", GITHUB_API_URL, owner, repo);

        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "token " + githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            return parseCommitData(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub commits", e);
        }
    }

    private List<GitHubCommitInfo> parseCommitData(String jsonData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode commitsNode = mapper.readTree(jsonData);

        List<GitHubCommitInfo> commits = new ArrayList<>();
        for (JsonNode commitNode : commitsNode) {
            GitHubCommitInfo commit = new GitHubCommitInfo();

            commit.setSha(commitNode.get("sha").asText());
            commit.setMessage(commitNode.get("commit").get("message").asText());
            commit.setAuthorName(commitNode.get("commit").get("author").get("name").asText());
            commit.setAuthorEmail(commitNode.get("commit").get("author").get("email").asText());
            commit.setDate(commitNode.get("commit").get("author").get("date").asText());
            commit.setUrl(commitNode.get("html_url").asText());

            commits.add(commit);
        }

        return commits;
    }
}
