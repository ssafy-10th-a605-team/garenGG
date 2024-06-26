package gg.garen.back.championRecommendation.controller;

import gg.garen.back.championRecommendation.service.ChampionRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/championRecommendation")
public class ChampionRecommendationController {

    private final ChampionRecommendationService championRecommendationService;

    @GetMapping("/{summonerName}-{tagLine}")
    ResponseEntity<?> getChampionRecommendation(@PathVariable("summonerName") String summonerName,
                                                @PathVariable("tagLine") String tagLine) {
        return championRecommendationService.getChampionRecommendation(summonerName, tagLine);
    }
}
