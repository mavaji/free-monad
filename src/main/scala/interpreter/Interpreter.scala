package interpreter

import monad.*
import pipeline.*

import java.util.UUID
import scala.annotation.tailrec
import scala.collection.mutable

object Kafka:
  def read(topic: String): Either[String, List[String]] =
    println(s"Reading from Kafka: $topic")
    // 50% chance of failure
    if Math.random() < 0.5 then
      Left(s"Failed to read from Kafka: $topic")
    else
      Right(List(
        s"event id:${UUID.randomUUID()} from $topic",
        s"event id:${UUID.randomUUID()} from $topic"
      ))

object Processor:
  def transform(records: List[String]): List[String] =
    println("Transforming records...")
    records.map(_.toUpperCase)

object Database:
  private val storage: mutable.ListBuffer[String] = mutable.ListBuffer()

  def write(records: List[String]): Either[String, Unit] =
    println(s"Writing ${records.length} records to the database.")
    // 50% chance of failure
    if Math.random() < 0.5 then
      Left(s"Error writing to database")
    else
      Database.storage ++= records
      Right(())


  def readAll(): List[String] =
    val allRecords = Database.storage.toList
    println(s"Database has ${allRecords.length} records")
    allRecords

object Interpreter extends (DataOp ~> IO):
  def apply[A](fa: DataOp[A]): IO[A] = fa match
    case ReadFromKafka(topic) => IO.create(Kafka.read(topic))

    case TransformData(records) => IO.create(Processor.transform(records))

    case WriteToDatabase(records) => IO.create(Database.write(records))

    case ReadAllFromDatabase() => IO.create(Database.readAll())

    case LogMessage(message) => IO.create(println(s"$message"))

    case Retryable(op, retries) => IO.create(retryOp(op, retries))

  @tailrec
  private def retryOp[A](op: DataOp[Either[String, A]], retries: Int): Either[String, A] =
    if retries <= 0 then
      Left("Max retries reached")
    else
      apply(op).unsafeRun() match
        case Right(result) => Right(result)
        case Left(err) =>
          println(s"Retrying due to error: $err")
          retryOp(op, retries - 1) // Retry recursively