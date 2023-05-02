package ru.vladbakumenko.ui.chat

import javafx.collections.{FXCollections, ObservableList}
import ru.vladbakumenko.dto.ChannelCompound
import ru.vladbakumenko.dto.ActorMessages.{GroupMessage, PrivateMessage}
import ru.vladbakumenko.ui.connection.ConnectionControllerModel

case class ChatControllerModel(
                                members: ObservableList[String] = FXCollections.observableArrayList(),
                                groupMessage: ObservableList[GroupMessage] = FXCollections.observableArrayList(),
                                privateMessage: ObservableList[PrivateMessage] = FXCollections.observableArrayList(),
                                var historyOfGroupMessage: List[GroupMessage] = List.empty,
                                var historyOfPrivateMessage: Map[ChannelCompound, List[PrivateMessage]] = Map.empty,
                                var tempList: List[PrivateMessage] = List.empty,
                                var connectionModel: ConnectionControllerModel = ConnectionControllerModel()
                              ) {
}

object ChatControllerModel {
  val GROUP_CHAT_NAME = "GROUP CHAT"
}
