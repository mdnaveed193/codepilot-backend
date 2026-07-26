// package com.extension.codepilot.service;

// import org.springframework.stereotype.Service;

// import com.extension.codepilot.dto.PushRequest;


// @Service
// public class ReadmeService {
// 	public String generateReadme(PushRequest pushRequest) {

//         StringBuilder readme = new StringBuilder();

//         readme.append("# ")
//               .append(pushRequest.getProblemTitle())
//               .append("\n\n");



//         readme.append("### Difficulty\n");
//         readme.append(pushRequest.getDifficulty())
//               .append("\n\n");

      

//         readme.append("## Problem Description\n\n");
//         readme.append(pushRequest.getDescription())
//               .append("\n\n");

//         readme.append("---\n\n");

//         // readme.append("Generated automatically using CodePilot Chrome Extension 🚀");

//         return readme.toString();
//     }

//     public String generateSolutionCommitMessage(PushRequest pushRequest) {

//         return "Add/Update solution for " + pushRequest.getProblemTitle();
//     }

//     public String generateReadmeCommitMessage(PushRequest pushRequest) {

//         return "Add/Update README for " + pushRequest.getProblemTitle();
//     }

// }
package com.extension.codepilot.service;

import org.springframework.stereotype.Service;

import com.extension.codepilot.dto.PushRequest;

@Service
public class ReadmeService {

    public String generateReadme(PushRequest pushRequest) {


        StringBuilder readme = new StringBuilder();

        readme.append("# ")
              .append(pushRequest.getProblemTitle())
              .append("\n\n");



        readme.append("\n\n");

        readme.append(getDifficultyBadge(pushRequest.getDifficulty()))
              .append("\n\n");

        readme.append("## Problem Description\n\n");
        readme.append(pushRequest.getDescription())
              .append("\n\n");


        return readme.toString();
    }

    public String generateSolutionCommitMessage(PushRequest pushRequest) {
        return "Add/Update solution for " + pushRequest.getProblemTitle();
    }

    public String generateReadmeCommitMessage(PushRequest pushRequest) {
        return "Add/Update README for " + pushRequest.getProblemTitle();
    }

    private String getDifficultyBadge(String difficulty) {

        if (difficulty == null || difficulty.isBlank()) {
            return "![Difficulty](https://img.shields.io/badge/Difficulty-Unknown-lightgrey)";
        }

        String normalizedDifficulty = difficulty.trim().toLowerCase();

        if (normalizedDifficulty.equals("easy")) {
            return "![Difficulty](https://img.shields.io/badge/Difficulty-Easy-00C853?style=for-the-badge&labelColor=1F2937)";
        }

        if (normalizedDifficulty.equals("medium")) {
            return "![Difficulty](https://img.shields.io/badge/Difficulty-Medium-fcc33a?style=for-the-badge&labelColor=1F2937)";
        }

        if (normalizedDifficulty.equals("hard")) {
            return "![Difficulty](https://img.shields.io/badge/Difficulty-Hard-fc3838?style=for-the-badge&labelColor=1F2937)";
        }

        return "![Difficulty](https://img.shields.io/badge/Difficulty-" 
                + difficulty.trim().replace(" ", "%20") 
                + "-lightgrey)";
    }
}