package co.com.bancolombia.model.simpsons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpsonsLocation {

    private Integer id;
    private String name;
    private String description;
}
