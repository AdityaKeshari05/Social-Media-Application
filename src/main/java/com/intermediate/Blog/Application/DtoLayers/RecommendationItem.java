package com.intermediate.Blog.Application.DtoLayers;

public class RecommendationItem {
    private long post_id;
    private double score;


    public long getPost_id() {
        return post_id;
    }

    public void setPost_id(long post_id) {
        this.post_id = post_id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
