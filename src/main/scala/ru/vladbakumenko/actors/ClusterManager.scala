package ru.vladbakumenko.actors

import akka.actor.{Actor, ActorLogging, Props}
import ru.vladbakumenko.dto.ActorMessages.{ChatMembers, GroupMessage, PrivateMessage, RemovedMember}
import ru.vladbakumenko.ui.chat.ChatControllerImpl

class ClusterManager(chatController: ChatControllerImpl) extends Actor with ActorLogging {
  override def receive: Receive = {
    case message: PrivateMessage =>
      chatController.addPrivateMessage(message)
    case message: GroupMessage =>
      chatController.addGroupMessage(message)
    case message: ChatMembers =>
      chatController.updateMembers(message)
    case message: RemovedMember =>
      chatController.removeMember(message)
  }
}

object ClusterManager {
  def getProps(controller: ChatControllerImpl): Props = Props.create(classOf[ClusterManager], controller)
}
