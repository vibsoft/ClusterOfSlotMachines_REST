package my.interview.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.interview.model.ClusterConfig;
import my.interview.model.SlotMachineClusterState;
import my.interview.model.SlotMachineState;
import my.interview.service.ClusterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
public class Controller {

  private ClusterService clusterService;

  @PostMapping(value = "/api/cluster", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineClusterState> clusterInitPost(
      @RequestBody ClusterConfig clusterConfig) {
    log.info("CONTROLLER - clusterInit - cluster config: {}", clusterConfig);

    return ResponseEntity.ok(clusterService.initCluster(clusterConfig));
  }

  @GetMapping(value = "/api/cluster/state", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineClusterState> clusterState() {
    log.info("CONTROLLER - clusterState");

    SlotMachineClusterState clusterState = clusterService.getClusterState();
    // log.info("CLUSTER - state: {}", clusterState);

    return ResponseEntity.ok(clusterState);
  }

  @GetMapping(
      value = "/api/cluster/machine/{id}/topup_tokens",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineState> machineTopupTokens(
      @PathVariable("id") Integer machineId, @RequestParam("tokenInput") Integer tokenInput) {
    log.info(
        "CONTROLLER - machineTopupTokens - machineId: {}, tokenInput: {}", machineId, tokenInput);

    return ResponseEntity.ok(clusterService.topupTokensSlotMachine(machineId, tokenInput));
  }

  @GetMapping(
      value = "/api/cluster/machine/{id}/pull_lever",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineState> pullLever(
      @PathVariable("id") Integer machineId,
      @RequestParam(value = "tokenInput", defaultValue = "1", required = false)
          Integer tokenInput) {
    log.info("CONTROLLER - pullLever - machineId: {}, tokenInput: {}", machineId, tokenInput);

    return ResponseEntity.ok(clusterService.pullLeverSlotMachine(machineId, tokenInput));
  }

  @GetMapping(
        value = "/api/cluster/machine/{id}/cash_out",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineState> cashOut(@PathVariable("id") Integer machineId) {
    log.info("CONTROLLER - cashOut - machineId: {}", machineId);

    SlotMachineState machineState = clusterService.cashOutSlotMachine(machineId);

    return ResponseEntity.ok(machineState);
  }

  @GetMapping(
      value = "/api/cluster/machine/{id}/state",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SlotMachineState> slotMachineState(@PathVariable("id") Integer machineId) {
    log.info("CONTROLLER - slotMachineState - machineId: {}", machineId);

    SlotMachineState machineState = clusterService.getSlotMachineState(machineId);
    // log.info("SLOT - state: {}", machineState);

    return ResponseEntity.ok(machineState);
  }
}
