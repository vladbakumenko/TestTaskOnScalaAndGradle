package ru.vladbakumenko.ui.connection

import akka.actor.{ActorRef, ActorSystem, Address}
import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.stage.Stage

case class ConnectionControllerModel(
                                      host: StringProperty = new SimpleStringProperty("127.0.0.1"),
                                      port: StringProperty = new SimpleStringProperty("255"),
                                      nickName: StringProperty = new SimpleStringProperty("")
                                    ) {
  var clusterListener: ActorRef = _
  var clusterAddress: Address = _
  var stage: Stage = _
  var system: ActorSystem = _
}
