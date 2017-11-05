package utils

/**
  * Created by neo on 05.11.17.
  */
case class SequenceNumber(timestamp: Long, label: String) extends Ordered[SequenceNumber] {
  override def compare(o: SequenceNumber): Int = {
    if (o.timestamp == timestamp) {
      label.compareTo(o.label)
    } else timestamp.compareTo(o.timestamp)
  }
}

object SequenceNumber {
  val min = SequenceNumber(0, "")
  def generate(label: String): SequenceNumber = SequenceNumber(System.currentTimeMillis, label)
}

