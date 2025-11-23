package co.com.bancolombia.mcp.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private boolean error;
    private String message;
    private String type;
}
