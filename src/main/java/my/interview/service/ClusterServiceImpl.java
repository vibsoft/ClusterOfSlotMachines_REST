package my.interview.service;

import lombok.extern.slf4j.Slf4j;
import my.interview.model.ClusterConfig;
import my.interview.model.SlotMachineClusterState;
import my.interview.model.SlotMachineState;
import my.interview.service.slotmachine.SlotMachineCluster;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClusterServiceImpl implements ClusterService {

  private SlotMachineCluster slotMachineCluster;

  public SlotMachineCluster getCluster() {
    if (slotMachineCluster == null) {
      log.error("SERVICE - Slot machine cluster is not initialized");
      throw new IllegalArgumentException("Slot machine cluster is not initialized");
    }

    return slotMachineCluster;
  }

  @Override
  public SlotMachineClusterState initCluster(ClusterConfig clusterConfig) {
    if (slotMachineCluster != null) {
      log.error(
          "SERVICE - Cluster of SlotMachines already existed: [clusterId: {}; slotMachines: {}]",
          slotMachineCluster.getClusterId(),
          slotMachineCluster.getSlotMachines().length);
      throw new IllegalArgumentException(
          String.format(
              "Cluster of slotMachines already existed: [clusterId: %s; slotMachines: %s]",
              slotMachineCluster.getClusterId(), slotMachineCluster.getSlotMachines().length));
    }

    slotMachineCluster =
        new SlotMachineCluster(clusterConfig.getClusterId(), clusterConfig.getSlotMachineCount());

    return slotMachineCluster.buildClusterState();
  }

  @Override
  public SlotMachineClusterState getClusterState() {
    return getCluster().buildClusterState();
  }

  @Override
  public SlotMachineState topupTokensSlotMachine(int machineId, int tokenInput) {
    return getCluster().topupTokens(machineId, tokenInput);
  }

  @Override
  public SlotMachineState pullLeverSlotMachine(int machineId, int tokenInput) {
    return getCluster().pullLever(machineId, tokenInput);
  }

  @Override
  public SlotMachineState getSlotMachineState(int machineId) {
    return getCluster().getSlotMachineState(machineId);
  }

  @Override
  public SlotMachineState cashOutSlotMachine(int machineId) {
    return getCluster().cashOutAllTokens(machineId);
  }
}
