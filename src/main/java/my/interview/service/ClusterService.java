package my.interview.service;

import my.interview.model.ClusterConfig;
import my.interview.model.SlotMachineClusterState;
import my.interview.model.SlotMachineState;

public interface ClusterService {

  SlotMachineClusterState initCluster(ClusterConfig clusterConfig);

  SlotMachineClusterState getClusterState();

  SlotMachineState topupTokensSlotMachine(int machineId, int tokenInput);

  SlotMachineState pullLeverSlotMachine(int machineId, int tokenInput);

  SlotMachineState getSlotMachineState(int machineId);

  SlotMachineState cashOutSlotMachine(int machineId);
}
