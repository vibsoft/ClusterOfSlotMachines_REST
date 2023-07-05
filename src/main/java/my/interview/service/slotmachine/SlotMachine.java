package my.interview.service.slotmachine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.interview.model.SlotMachineState;
import my.interview.model.SpinResult;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlotMachine {

  private String machineId;
  private final AtomicInteger tokenCredit = new AtomicInteger(0);

  private int addTokenCredit(int addTokenCredit) {
    return tokenCredit.addAndGet(addTokenCredit);
  }

  public int getTokenCredit() {
    return tokenCredit.get();
  }

  public SlotMachineState topupTokens(int tokenInput) {
    addTokenCredit(tokenInput);

    log.info(
        "SLOT - TopUp [machineId:{}, tokenInput: {}, tokenCredit: {}]",
        machineId,
        tokenInput,
        getTokenCredit());

    return buildSlotMachineTokenState();
  }

  public SlotMachineState cashOutAllTokens() {
    int cashOutTokens = getTokenCredit();
    return cashOutTokens(cashOutTokens);
  }

  private SlotMachineState cashOutTokens(int cashOutTokens) {
    addTokenCredit(-cashOutTokens);

    log.info("SLOT - CashOut [machineId:{}, cashOutTokens: {}]", machineId, cashOutTokens);

    return SlotMachineState.builder()
        .machineId(machineId)
        .cashOut(cashOutTokens)
        .tokenCredit(getTokenCredit())
        .build();
  }

  public SlotMachineState pullLever(int tokenInput) {
    int tokenCredit = getTokenCredit();
    if (tokenInput > getTokenCredit()) {
      log.error(
          "SLOT - Insufficient Token Balance To Pull Lever. [machineId:{}, tokenInput: {}, tokenCredit: {}]",
          machineId,
          tokenInput,
          tokenCredit);
      throw new IllegalArgumentException(
          String.format(
              "Insufficient Token Balance To Pull Lever [machineId: %s, tokenInput: %s, tokenCredit: %s]",
              machineId, tokenInput, tokenCredit));
    }

    SpinResult spinResult = doSpinDraw();

    return processSpinDraw(tokenInput, spinResult);
  }

  private SpinResult doSpinDraw() {
    Random generator = new Random();

    return SpinResult.builder()
        .spin1(generator.nextInt(10))
        .spin2(generator.nextInt(10))
        .spin3(generator.nextInt(10))
        .build();
  }

  private SlotMachineState processSpinDraw(int tokenInput, SpinResult spinResult) {
    String message;
    if ((spinResult.getSpin1() == 0)
        && (spinResult.getSpin2() == 0)
        && (spinResult.getSpin3() == 0)) {

      message = "Super Jackpot Winner";
      addTokenCredit(500 * tokenInput);

    } else if ((spinResult.getSpin1() == spinResult.getSpin2())
        && (spinResult.getSpin2() == spinResult.getSpin3())
        && (spinResult.getSpin3() == spinResult.getSpin1())) {

      message = "Jackpot Winner";
      addTokenCredit(50 * tokenInput);

    } else if ((spinResult.getSpin1() == spinResult.getSpin2())
        && (spinResult.getSpin1() != spinResult.getSpin3())) {

      message = "Free Spin";
      addTokenCredit(tokenInput);

    } else if ((spinResult.getSpin1() != spinResult.getSpin2())
        && (spinResult.getSpin2() != spinResult.getSpin3())
        && (spinResult.getSpin3() != spinResult.getSpin1())) {

      message = "Bad Luck";
      addTokenCredit(-tokenInput);

    } else {
      message = "Play Again";
    }

    log.info(
        "SLOT - Pull Lever [ machineId: {}, tokenInput: {}, message: {}, spinResult: {}]",
        machineId,
        tokenInput,
        message,
        spinResult);

    return SlotMachineState.builder()
        .machineId(machineId)
        .message(message)
        .spinResult(spinResult)
        .tokenCredit(getTokenCredit())
        .build();
  }

  public SlotMachineState buildSlotMachineTokenState() {
    return SlotMachineState.builder()
        .machineId(getMachineId())
        .tokenCredit(getTokenCredit())
        .build();
  }
}
