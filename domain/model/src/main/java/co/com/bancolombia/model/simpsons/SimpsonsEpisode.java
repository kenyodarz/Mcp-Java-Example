package co.com.bancolombia.model.simpsons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpsonsEpisode {

    private Integer id;
    private String airdate;
    private String description;
    private Integer episodeNumber;
    private String imagePath;
    private String name;
    private Integer season;
    private String synopsis;
}
