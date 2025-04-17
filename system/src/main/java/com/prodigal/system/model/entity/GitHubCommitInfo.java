package com.prodigal.system.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-picture
 * @date 2025/4/16 9:50
 * @description: GitHub提交信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubCommitInfo implements Serializable {
    private static final long serialVersionUID = 3412967493207460593L;
    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private String date;
    private String url;
}
