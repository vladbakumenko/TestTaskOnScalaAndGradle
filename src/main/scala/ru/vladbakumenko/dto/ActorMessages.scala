package ru.vladbakumenko.dto

import akka.actor.Address

object ActorMessages {
  trait MessageTrait {val senderName: String; val value: String}
  case class GroupMessage(senderName: String, value: String) extends MessageTrait
  case class PrivateMessage(senderName: String, value: String, recipientName: String) extends MessageTrait
  case class Connection(nickName: String, connectionAddress: Address, userAddress: Address)
  case class ChatMembers(var members: Set[Connection])
  case class RemovedMember(val name: String)
}
