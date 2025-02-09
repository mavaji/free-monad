package interpreter

import monad.*
import pipeline.*

import java.util.UUID
import scala.annotation.tailrec
import scala.collection.mutable

object Database:
  private val storage: mutable.ListBuffer[String] = mutable.ListBuffer()

  def write(records: List[String]): Either[String, Unit] =
    // 50% chance of failure
    if Math.random() < 0.5 then
      Left(s"Error writing to database")
    else
      Database.storage ++= records
      Right(())

  def readAll(): List[String] = Database.storage.toList

object Kafka:
  def read(topic: String): Either[String, List[String]] =
    // 50% chance of failure
    if Math.random() < 0.5 then
      Left(s"Failed to read from Kafka: $topic")
    else
      Right(List(
        s"event id:${UUID.randomUUID()} from $topic",
        s"event id:${UUID.randomUUID()} from $topic"
      ))

object Interpreter extends (DataOp ~> Id):
  def apply[A](fa: DataOp[A]): Id[A] = fa match
    case ReadFromKafka(topic) => readFromKafka(topic)

    case TransformData(records) => transformData(records)

    case WriteToDatabase(records) => writeToDatabase(records)

    case ReadAllFromDatabase() => readAllFromDatabase()

    case LogMessage(message) => log(message)

    case Retryable(op, retries) => retryOp(op, retries)

  private def readFromKafka[A](topic: String): Either[String, List[String]] =
    println(s"Reading from Kafka: $topic")
    Kafka.read(topic)

  private def transformData[A](records: List[String]): List[String] =
    println("Transforming records...")
    records.map(_.toUpperCase)

  private def writeToDatabase[A](records: List[String]): Either[String, Unit] =
    println(s"Writing ${records.length} records to the database.")
    Database.write(records)

  private def readAllFromDatabase[A](): List[String] =
    val allRecords = Database.readAll()
    println(s"Database has ${allRecords.length} records")
    allRecords


  private def log[A](message: String): Unit = println(s"$message")

  @tailrec
  private def retryOp[A](op: DataOp[Either[String, A]], retries: Int): Either[String, A] =
    if retries <= 0 then
      Left("Max retries reached")
    else
      apply(op) match
        case Right(result) => Right(result)
        case Left(err) =>
          println(s"Retrying due to error: $err")
          retryOp(op, retries - 1) // Retry recursively
