package com.portfolio.jobtracker.dto;

import java.util.List;

public record AiAnalysisResponse(
    Integer score,
    List<String> missingSkills
) {}
