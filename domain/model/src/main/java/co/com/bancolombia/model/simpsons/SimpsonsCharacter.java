package co.com.bancolombia.model.simpsons;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpsonsCharacter {

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
}
