package co.com.bancolombia.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpsonsEpisodeResponse {

    private Integer id;
    private String airdate;
    private String description;
    @JsonProperty("episode_number")
    private Integer episodeNumber;
    @JsonProperty("image_path")
    private String imagePath;
    private String name;
    private Integer season;
    private String synopsis;
}
