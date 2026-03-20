package com.intermediate.Blog.Application.DtoLayers;

import java.util.List;

public class RecommendationsResponse {

    private List<RecommendationItem> recommendations;

    public List<RecommendationItem> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }
}
