package my.interview.service.slotmachine;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import my.interview.model.SlotMachineClusterState;
import my.interview.model.SlotMachineState;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
public class SlotMachineCluster {
  private String clusterId;
  private static AtomicInteger houseCredit = new AtomicInteger(0);
  private SlotMachine[] slotMachines;

  public SlotMachineCluster(String clusterId, int slotMachineCount) {
    this.clusterId = clusterId;
    this.initClusterMachines(slotMachineCount);
  }

  private void initClusterMachines(int slotMachineCount) {
    if (slotMachineCount < 1) {
      log.error("CLUSTER - Illegal slot machine count - {}", slotMachineCount);
      throw new IllegalArgumentException(
          String.format("Illegal slot machine count - %s", slotMachineCount));
    }

    this.slotMachines = new SlotMachine[slotMachineCount];
    for (int i = 0; i < slotMachineCount; i++) {
      slotMachines[i] = SlotMachine.builder().machineId(String.valueOf(i)).build();
    }
  }

  private int addHouseCredit(int tokenCredit) {
    return houseCredit.addAndGet(tokenCredit);
  }

  public int getHouseCredit() {
    return houseCredit.get();
  }

  private SlotMachine getSlotMachine(int machineId) {
    if (machineId > slotMachines.length - 1) {
      throw new IllegalArgumentException(
          String.format(
              "Illegal machineId: %s, cluster size: %s, machine Ids should be in [0, ... ,%s] ",
              machineId, slotMachines.length, slotMachines.length - 1));
    }

    return slotMachines[machineId];
  }

  public SlotMachineState topupTokens(int machineId, int tokenInput) {
    SlotMachine slotMachine = getSlotMachine(machineId);
    addHouseCredit(tokenInput);

    return slotMachine.topupTokens(tokenInput);
  }

  public SlotMachineState pullLever(int machineId, int tokenInput) {
    SlotMachine slotMachine = getSlotMachine(machineId);

    return slotMachine.pullLever(tokenInput);
  }

  public SlotMachineState cashOutAllTokens(int machineId) {
    SlotMachine machine = getSlotMachine(machineId);

    return machine.cashOutAllTokens();
  }

  public SlotMachineState getSlotMachineState(int machineId) {
    SlotMachine slotMachine = getSlotMachine(machineId);

    return slotMachine.buildSlotMachineTokenState();
  }

  public SlotMachineClusterState buildClusterState() {
    List<SlotMachineState> machinesState =
        Stream.of(slotMachines)
            .map(SlotMachine::buildSlotMachineTokenState)
            .collect(Collectors.toList());

    return SlotMachineClusterState.builder()
        .clusterId(clusterId)
        .houseCredit(getHouseCredit())
        .machineState(machinesState)
        .build();
  }
}
