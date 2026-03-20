package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.DtoLayers.RecommendationItem;
import com.intermediate.Blog.Application.DtoLayers.RecommendationRequest;
import com.intermediate.Blog.Application.DtoLayers.RecommendationsResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RecommendationClient {


    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8000";

    public RecommendationClient(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
    }


    public List<Long> getRecommendedPostIds(long userId , int topN , double alpha){
        RecommendationRequest req = new RecommendationRequest();
        req.setUser_id(userId);
        req.setAlpha(alpha);
        req.setTopN(topN);

        try{

            RecommendationsResponse  resp = restTemplate.postForObject(
                    baseUrl + "/recommendations",
                    req,
                    RecommendationsResponse.class
            );
            if(resp == null || resp.getRecommendations() ==null){
                return List.of();
            }
            return resp.getRecommendations()
                    .stream()
                    .map(RecommendationItem::getPost_id)
                    .toList();

        }catch (Exception e){
            return List.of();
        }
    }
}
