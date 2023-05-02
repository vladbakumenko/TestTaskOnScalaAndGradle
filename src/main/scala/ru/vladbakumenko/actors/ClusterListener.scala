package ru.vladbakumenko.actors

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.Cluster
import ru.vladbakumenko.dto.ActorMessages.{ChatMembers, Connection, GroupMessage, PrivateMessage, RemovedMember}

class ClusterListener(cluster: Cluster) extends Actor with ActorLogging {

  private val chatMembers = ChatMembers(Set.empty)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member)

      cluster.state.members
        .foreach(member => context.actorSelection(member.address.toString + "/user/listener") ! chatMembers)

    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address.toString, previousStatus)

      val removedConnection = chatMembers.members
        .find(c => c.userAddress == member.address)
        .getOrElse(throw new RuntimeException())
      chatMembers.members = chatMembers.members -- Set(removedConnection)

      val memberRemoved = RemovedMember(removedConnection.nickName)

      cluster.state.members
        .foreach(member => sendMessageToManager(member.address, memberRemoved))

    case message: PrivateMessage =>
      val sender = getAddress(message.senderName)
      val recipient = getAddress(message.recipientName)

      if (sender == recipient) sendMessageToManager(sender, message)
      else {
        sendMessageToManager(sender, message)
        sendMessageToManager(recipient, message)
      }

    case message: GroupMessage =>
      cluster.state.members
        .foreach(member => sendMessageToManager(member.address, message))

    case connection: Connection =>
      cluster.join(connection.connectionAddress)
      chatMembers.members = chatMembers.members ++ Set(connection)

    case message: ChatMembers =>
      chatMembers.members = chatMembers.members ++ message.members
      cluster.state.members
        .foreach(member => sendMessageToManager(member.address, chatMembers))
  }

  private def getAddress(name: String): Address = {
    chatMembers.members
      .find(_.nickName == name)
      .map(_.userAddress)
      .getOrElse(throw new RuntimeException())
  }

  private def sendMessageToManager[T](address: Address, message: T): Unit = {
    context.actorSelection(address.toString + "/user/manager") ! message
  }
}

object ClusterListener {
  def getProps(cluster: Cluster): Props = Props.create(classOf[ClusterListener], cluster)
}
