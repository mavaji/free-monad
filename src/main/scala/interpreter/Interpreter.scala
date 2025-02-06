package interpreter

import monad.{Id, ~>}
import pipeline.*

import java.util.UUID
import scala.collection.mutable

object Database {
  val storage: mutable.ListBuffer[String] = mutable.ListBuffer()
}

object Kafka {
  def read(topic: String): List[String] =
    List(
      s"event id:${UUID.randomUUID()} from $topic",
      s"event id:${UUID.randomUUID()} from $topic"
    )
}

object Interpreter extends (DataOp ~> Id) {
  def apply[A](fa: DataOp[A]): Id[A] = fa match {
    case ReadFromKafka(topic) =>
      println(s"Reading from Kafka: $topic")
      Kafka.read(topic)

    case TransformData(records) =>
      println("Transforming records...")
      records.map(_.toUpperCase)

    case WriteToDatabase(records) =>
      println(s"Writing ${records.length} records to the database.")
      Database.storage ++= records
      Right(())

    case LogMessage(message) =>
      println(s"LOG: $message")
  }
}

object InterpreterWithError extends (DataOp ~> Id) {
  def apply[A](fa: DataOp[A]): Id[A] = fa match {
    case ReadFromKafka(topic) =>
      println(s"Reading from Kafka: $topic")
      Kafka.read(topic)

    case TransformData(records) =>
      println("Transforming records...")
      records.map(_.toUpperCase)

    case WriteToDatabase(records) =>
      Left(s"Error writing to database")

    case LogMessage(message) =>
      println(s"LOG: $message")
  }
}