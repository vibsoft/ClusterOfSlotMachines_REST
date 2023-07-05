package my.interview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotMachineState {
  private String machineId;
  private SpinResult spinResult;
  private String message;
  private Integer cashOut;
  private Integer tokenCredit;
}
