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
    private static final int PAGE_SIZE = 100;
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
        List<GitHubCommitInfo> allCommits = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "token " + githubToken);//私有仓库需要 token
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            int page = 1;
            boolean hasMoreCommits = true;
            while (hasMoreCommits) {
                // 构造分页 URL
                String urlWithPage = url + "?per_page=" + PAGE_SIZE + "&page=" + page;
                ResponseEntity<String> response = restTemplate.exchange(
                        urlWithPage,
                        HttpMethod.GET,
                        entity,
                        String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode commitsNode = new ObjectMapper().readTree(response.getBody());

                    if (commitsNode != null && commitsNode.isArray()) {
                        for (JsonNode commitNode : commitsNode) {
                            GitHubCommitInfo commit = convertToCommitInfo(commitNode);
                            if (commit != null) {
                                allCommits.add(commit);
                            }
                        }
                        // 如果当前页的记录数少于 PAGE_SIZE，则表示没有更多记录
                        hasMoreCommits = commitsNode.size() == PAGE_SIZE;
                    } else {
                        hasMoreCommits = false;
                    }
                } else {
                    hasMoreCommits = false;
                }

                page++;

            }
            return allCommits;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub commits", e);
        }
    }

    private GitHubCommitInfo convertToCommitInfo(JsonNode commitNode) {
        if (commitNode == null || !commitNode.has("sha") || !commitNode.has("commit")) {
            return null;
        }

        GitHubCommitInfo commit = new GitHubCommitInfo();
        commit.setSha(commitNode.get("sha").asText());
        commit.setMessage(commitNode.get("commit").get("message").asText());
        commit.setAuthorName(commitNode.get("commit").get("author").get("name").asText());
        commit.setAuthorEmail(commitNode.get("commit").get("author").get("email").asText());
        commit.setDate(commitNode.get("commit").get("author").get("date").asText());
        commit.setUrl(commitNode.get("html_url").asText());

        return commit;
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
