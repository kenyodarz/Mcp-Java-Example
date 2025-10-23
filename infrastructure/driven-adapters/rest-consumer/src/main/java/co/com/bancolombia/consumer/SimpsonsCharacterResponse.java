package co.com.bancolombia.consumer;

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
    private String gender;
    private String name;
    private String occupation;
    private List<String> phrases;
    private String portraitPath;
    private String status;
}
