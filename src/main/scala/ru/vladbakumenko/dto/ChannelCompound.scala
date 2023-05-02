package ru.vladbakumenko.dto

case class ChannelCompound(member1: String, member2: String) {
  override def equals(obj: Any): Boolean = obj match {
    case o: ChannelCompound =>
      (member1 == o.member1 && member2 == o.member2) ||
        (member2 == o.member1 && member1 == o.member2) ||
        (member1 == o.member2 && member2 == o.member1)
    case _ => false
  }

  override def hashCode(): Int = member1.hashCode + member2.hashCode
}
