package pipeline

import monad.Free

object DataPipeline {
  private type DataOpF[A] = Free[DataOp, A]

  private def readFromKafka(topic: String): DataOpF[List[String]] =
    Free.liftF(ReadFromKafka(topic))

  private def transformData(records: List[String]): DataOpF[List[String]] =
    Free.liftF(TransformData(records))

  private def writeToDatabase(records: List[String]): DataOpF[Either[String, Unit]] =
    Free.liftF(WriteToDatabase(records))

  private def logMessage(msg: String): DataOpF[Unit] =
    Free.liftF(LogMessage(msg))

  def processPipeline(topic: String): DataOpF[Unit] = for {
    _ <- logMessage(s"Starting processing for topic: $topic")
    records <- readFromKafka(topic)
    _ <- logMessage(s"Read ${records.length} records:\n\t${records.mkString("\n\t")}")
    cleaned <- transformData(records)
    _ <- logMessage(s"Transformed ${cleaned.length} records")
    dbResult <- writeToDatabase(cleaned)
    - <- dbResult match
      case Right(_) =>
        for {
          _ <- logMessage("Write to DB successful")
          _ <- logMessage("Processing complete")
        } yield ()
      case Left(err) =>
        for {
          _ <- logMessage(s"Write to DB failed: $err")
          _ <- logMessage("Processing failed")
        } yield ()
  } yield ()
}
