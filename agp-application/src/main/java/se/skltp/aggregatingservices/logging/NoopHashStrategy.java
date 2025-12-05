package se.skltp.aggregatingservices.logging;

public class NoopHashStrategy implements HashStrategy {
  @Override
  public String hash(String input) {
    return input == null ? "" : input;
  }
}

