package ru.vladbakumenko.ui.connection

import akka.actor.Address
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.{Button, Label}
import javafx.scene.layout.{AnchorPane, VBox}
import javafx.stage.Stage
import ru.vladbakumenko.actors.ClusterManager
import ru.vladbakumenko.ConnectionController
import ru.vladbakumenko.dto.ActorMessages.Connection
import ru.vladbakumenko.ui.chat.ChatControllerImpl

class ConnectionControllerImpl extends ConnectionController {

  private val connectionModel: ConnectionControllerModel = new ConnectionControllerModel

  private final val defaultUserName = "username-" + System.currentTimeMillis()

  def getConnectionModel: ConnectionControllerModel = connectionModel

  override def initialize(): Unit = {
    host.textProperty().bindBidirectional(connectionModel.host)
    port.textProperty().bindBidirectional(connectionModel.port)
    nickname.textProperty().bindBidirectional(connectionModel.nickName)

    connect.setOnAction(_ => {
      if (connectionModel.nickName.getValue.isEmpty) connectionModel.nickName.setValue(defaultUserName)
      if (connectionModel.host.getValue.isEmpty || connectionModel.port.getValue.isEmpty) {
        showCautionWindow()
        throw new RuntimeException("Empty host or port")
      }

      val address = Address("akka", "ClusterSystem", connectionModel.host.getValue, connectionModel.port.getValue.toInt)
      val connection = Connection(connectionModel.nickName.getValue, address, connectionModel.clusterAddress)

      showChatWindow()

      connectionModel.clusterListener ! connection
      connectionModel.stage.close()
    })
  }

  private def showChatWindow(): Unit = {
    val loader = new FXMLLoader()
    loader.setLocation(getClass.getResource("/fxml/Chat.fxml"))
    val rootLayout: AnchorPane = loader.load
    val scene = new Scene(rootLayout, 650, 500)

    val controller: ChatControllerImpl = loader.getController
    controller.getChatModel.connectionModel = connectionModel
    connectionModel.system.actorOf(ClusterManager.getProps(controller), "manager")

    val stage: Stage = new Stage
    stage.setScene(scene)
    stage.setTitle("Твой никнейм: " + connectionModel.nickName.getValue)
    stage.show()
    stage.setOnCloseRequest(_ => connectionModel.system.terminate())
  }

  private def showCautionWindow(): Unit = {
    val label: Label = new Label("Не введён хост или порт")
    val button: Button = new Button("OK")
    val pane: VBox = new VBox(label, button)
    pane.setAlignment(Pos.CENTER)
    pane.setSpacing(20)

    val scene: Scene = new Scene(pane, 200, 100)
    val stage: Stage = new Stage
    stage.setScene(scene)
    stage.show()

    button.setOnAction(_ => stage.close())
  }
}
