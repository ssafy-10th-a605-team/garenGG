package gg.garen.back.matchPrediction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ParticipantDto {
    private int assistMePings;
    private String championName;
    private String individualPosition;
    private String summonerName;
    private String riotIdTagline;
    private int kills;
    private int deaths;
    private boolean win;
}
