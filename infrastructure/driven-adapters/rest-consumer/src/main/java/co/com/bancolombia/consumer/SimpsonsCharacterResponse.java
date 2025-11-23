package co.com.bancolombia.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SimpsonsCharacterResponse {

    private Integer id;
    private Integer age;
    private String birthdate;
    private String description;
    @JsonProperty("first_appearance_ep_id")
    private Integer firstAppearanceEpId;
    @JsonProperty("first_appearance_sh_id")
    private Integer firstAppearanceShId;
    private String gender;
    private String name;
    private String occupation;
    private List<String> phrases;
    @JsonProperty("portraitPath")
    private String portraitPath;
    private String status;
    @JsonProperty("first_appearance_ep")
    private EpisodeResponse firstAppearanceEp;
    @JsonProperty("first_appearance_sh")
    private ShortResponse firstAppearanceSh;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EpisodeResponse {

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShortResponse {

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
}
