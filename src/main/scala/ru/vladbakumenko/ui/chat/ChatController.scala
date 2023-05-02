package ru.vladbakumenko.ui.chat

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ListChangeListener
import javafx.scene.input.{KeyCode, KeyEvent}
import ru.vladbakumenko.ChatController
import ru.vladbakumenko.dto.ActorMessages.{ChatMembers, GroupMessage, MessageTrait, PrivateMessage, RemovedMember}
import ru.vladbakumenko.dto.{ActorMessages, ChannelCompound}
import ru.vladbakumenko.ui.chat.ChatControllerModel.GROUP_CHAT_NAME

import scala.jdk.CollectionConverters._


class ChatControllerImpl extends ChatController {

  private val chatModel = ChatControllerModel()

  def getChatModel: ChatControllerModel = chatModel

  override def initialize(): Unit = {
    chatModel.members.add(GROUP_CHAT_NAME)
    listViewOfMembers.setItems(chatModel.members)
    listViewOfMembers.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        if (newValue == GROUP_CHAT_NAME) {
          addHistoryToLog(chatModel.historyOfGroupMessage)
        } else {
          val currentChanelCompound = ChannelCompound(newValue, chatModel.connectionModel.nickName.getValue)
          val currentHistory = chatModel.historyOfPrivateMessage
            .getOrElse(currentChanelCompound, List.empty)
          addHistoryToLog(currentHistory)
        }
      }
    })

    chatModel.groupMessage.addListener(new ListChangeListener[GroupMessage] {
      override def onChanged(c: ListChangeListener.Change[_ <: GroupMessage]): Unit = {
        val message: GroupMessage = c.getList.get(0)
        if (GROUP_CHAT_NAME == getSelectedNameOfChat) addMessageToLog(message)
        chatModel.groupMessage.remove(0)

        chatModel.historyOfGroupMessage = chatModel.historyOfGroupMessage ++ List(message)
      }
    })

    chatModel.privateMessage.addListener(new ListChangeListener[PrivateMessage] {
      override def onChanged(c: ListChangeListener.Change[_ <: PrivateMessage]): Unit = {
        val message: PrivateMessage = c.getList.get(0)
        if (message.senderName == getSelectedNameOfChat || message.recipientName == getSelectedNameOfChat)
          addMessageToLog(message)
        chatModel.privateMessage.remove(0)

        addMessageToPrivateHistory(message)
      }
    })
  }

  def enterKeyPressed(): Unit = {
    messageField.setOnKeyPressed((event: KeyEvent) => {
      if (event.getCode == KeyCode.ENTER) {
        val text = messageField.getText
        if (GROUP_CHAT_NAME == getSelectedNameOfChat) {
          val message = GroupMessage(getNickName, text)
          chatModel.connectionModel.clusterListener ! message
        } else {
          val message = PrivateMessage(getNickName, text, getSelectedNameOfChat)
          chatModel.connectionModel.clusterListener ! message
        }
        messageField.setText("")
      }
    })
  }

  def addPrivateMessage(message: PrivateMessage): Unit = {
    Platform.runLater(() => {
      chatModel.privateMessage.add(message)
    })
  }

  def addGroupMessage(message: GroupMessage): Unit = {
    Platform.runLater(() => {
      chatModel.groupMessage.add(message)
    })
  }

  def updateMembers(message: ChatMembers): Unit = {
    Platform.runLater(() => {
      val newMembers = message.members.map(c => c.nickName)
      val members = chatModel.members.asScala.toSet ++ newMembers
      chatModel.members.clear()
      members.foreach(m => chatModel.members.add(m))
    })
  }

  def removeMember(message: RemovedMember): Unit = {
    Platform.runLater(() => {
      chatModel.members.remove(message.name)
    })
  }

  private def addHistoryToLog(history: List[MessageTrait]): Unit = {
    val historyLog = history.map(message => s"${message.senderName}: ${message.value}\n").mkString("")
    logArea.clear()
    logArea.setText(historyLog)
  }

  private def getSelectedNameOfChat: String = {
    Option(listViewOfMembers.getSelectionModel.getSelectedItem) match {
      case Some(selectedName) => selectedName
      case None => GROUP_CHAT_NAME
    }
  }

  private def addMessageToPrivateHistory(msg: PrivateMessage): Unit = {
    val compound: ChannelCompound = ChannelCompound(msg.senderName, msg.recipientName)

    val resultList: List[PrivateMessage] = chatModel.historyOfPrivateMessage.get(compound) match {
      case Some(list) => list :+ msg
      case None => List(msg)
    }
    chatModel.historyOfPrivateMessage = chatModel.historyOfPrivateMessage + (compound -> resultList)
  }

  private def addMessageToLog(message: MessageTrait): Unit =
    logArea.appendText(message.senderName + ": " + message.value + "\n")

  private def getNickName: String = chatModel.connectionModel.nickName.getValue
}
