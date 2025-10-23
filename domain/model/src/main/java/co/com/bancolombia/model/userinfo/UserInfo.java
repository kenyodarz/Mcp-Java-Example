package co.com.bancolombia.model.userinfo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Integer id;
    private Integer age;
    private String birthdate;
    private String description;
    private String gender;
    private String name;
    private String occupation;
    private String portraitPath;
    private String status;
    private List<String> phrases;
    private Episode firstAppearanceEp;
    private ShortInfo firstAppearanceSh;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Episode {

        private Integer id;
        private String airdate;
        private String description;
        private Integer episodeNumber;
        private String imagePath;
        private String name;
        private Integer season;
        private String synopsis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShortInfo {

        private Integer id;
        private String airdate;
        private String description;
        private Integer episodeNumber;
        private String imagePath;
        private String name;
        private Integer season;
        private String synopsis;
    }
}
