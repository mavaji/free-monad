package pipeline

sealed trait DataOp[A]

case class ReadFromKafka(topic: String) extends DataOp[List[String]]

case class TransformData(records: List[String]) extends DataOp[List[String]]

case class WriteToDatabase(records: List[String]) extends DataOp[Either[String, Unit]]

case class LogMessage(message: String) extends DataOp[Unit]