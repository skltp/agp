package se.skltp.aggregatingservices.service;

import lombok.Data;

@Data
public class Authority {
  String senderId;
  String originalSenderId;
  String receiverId;
  String servicecontractNamespace;
}
