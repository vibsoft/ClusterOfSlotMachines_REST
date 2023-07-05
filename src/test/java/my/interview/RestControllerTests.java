package my.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.interview.model.ClusterConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@Slf4j
public class RestControllerTests {
  @Autowired private MockMvc mockMvc;

  @Test
  public void testSlotMachineCluster_Scenario_1() throws Exception {
    int clusterSize = 3; // MachineId: [0,1,2]

    // Cluster Init - testApiCluster_Init(int size)
    testApiCluster_Init("cluster_0", clusterSize)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.machineState[0].machineId").value("0"))
        .andExpect(jsonPath("$.machineState[2].machineId").value("2"))
        .andExpect(jsonPath("$.machineState[3].machineId").doesNotExist());

    // Cluster State - testApiCluster_State()
    testApiCluster_State();

    // PullLever without credits - testApiCluster_PullLever(int machineId, int tokenInput)
    testApiCluster_PullLever(0, 1)
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.errorReason")
                .value("Insufficient Token Balance To Pull Lever [machineId: 0, tokenInput: 1, tokenCredit: 0]"));
    testApiCluster_PullLever(2, 1)
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.errorReason")
                .value("Insufficient Token Balance To Pull Lever [machineId: 2, tokenInput: 1, tokenCredit: 0]"));

    // TopupTokens - testApiMachine_Topup(int machineId, int tokenInput)
    testApiMachine_Topup(0, 10)
        .andExpect(jsonPath("$.machineId").value(0))
        .andExpect(jsonPath("$.tokenCredit").value("10"))
        .andExpect(status().isOk());

    testApiMachine_Topup(1, 20)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.machineId").value(1))
        .andExpect(jsonPath("$.tokenCredit").value("20"));

    testApiMachine_Topup(2, 30)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.machineId").value(2))
        .andExpect(jsonPath("$.tokenCredit").value("30"));
    testApiCluster_State();

    // PullLever with tokens - testApiCluster_PullLever(int machineId, int tokenInput)
    testApiCluster_PullLever(0, 1).andExpect(status().isOk());
    testApiCluster_PullLever(0, 1).andExpect(status().isOk());
    testApiCluster_PullLever(0, 1).andExpect(status().isOk());
    testApiCluster_PullLever(1, 2).andExpect(status().isOk());
    testApiCluster_PullLever(1, 2).andExpect(status().isOk());
    testApiCluster_PullLever(1, 2).andExpect(status().isOk());
    testApiCluster_PullLever(2, 3).andExpect(status().isOk());
    testApiCluster_PullLever(2, 3).andExpect(status().isOk());
    testApiCluster_PullLever(2, 3).andExpect(status().isOk());
    // testApiCluster_State();

    // CashOut - testApiMachine_CashOut(int machineId)
    testApiMachine_CashOut(0)
        .andExpect(jsonPath("$.machineId").value(0))
        .andExpect(jsonPath("$.cashOut").isNotEmpty())
        .andExpect(jsonPath("$.tokenCredit").value("0"))
        .andExpect(status().isOk());
    testApiMachine_CashOut(1)
        .andExpect(jsonPath("$.machineId").value(1))
        .andExpect(jsonPath("$.cashOut").isNotEmpty())
        .andExpect(jsonPath("$.tokenCredit").value("0"));
    testApiMachine_CashOut(2)
        .andExpect(jsonPath("$.machineId").value(2))
        .andExpect(jsonPath("$.cashOut").isNotEmpty())
        .andExpect(jsonPath("$.tokenCredit").value("0"));
    // testApiCluster_State();
  }

  public ResultActions testApiCluster_Init(String clusterId, int machinesCount) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ClusterConfig clusterConfig =
        ClusterConfig.builder().clusterId(clusterId).slotMachineCount(machinesCount).build();

    return mockMvc
        .perform(
            post("/api/cluster")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clusterConfig)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    "/api/cluster/init",
                    mvcResult.getResponse().getContentAsString()));
  }

  public ResultActions testApiCluster_State() throws Exception {
    return mockMvc
        .perform(get("/api/cluster/state"))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    "/api/cluster/state",
                    mvcResult.getResponse().getContentAsString()))
        .andExpect(status().isOk());
  }

  public ResultActions testApiCluster_PullLever(int machineId, int tokenInput) throws Exception {
    return mockMvc
        .perform(
            get(
                String.format(
                    "/api/cluster/machine/%s/pull_lever?tokenInput=%s", machineId, tokenInput)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    String.format(
                        "/api/cluster/machine/%s/pull_lever?tokenInput=%s", machineId, tokenInput),
                    mvcResult.getResponse().getContentAsString()));
  }

  public ResultActions testApiMachine_Topup(int machineId, int tokenInput) throws Exception {
    return mockMvc
        .perform(
            get(
                String.format(
                    "/api/cluster/machine/%s/topup_tokens?tokenInput=%s", machineId, tokenInput)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    String.format(
                        "/api/cluster/machine/%s/topup_tokens?tokenInput=%s",
                        machineId, tokenInput),
                    mvcResult.getResponse().getContentAsString()));
  }

  public ResultActions testApiMachine_CashOut(int machineId) throws Exception {
    return mockMvc
        .perform(get(String.format("/api/cluster/machine/%s/cash_out", machineId)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    String.format("/api/cluster/machine/%s/cash_out", machineId),
                    mvcResult.getResponse().getContentAsString()));
  }
}
