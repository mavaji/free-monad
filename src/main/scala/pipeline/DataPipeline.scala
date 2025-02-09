package pipeline

import monad.Free

object DataPipeline:
  private type DataOpFree[A] = Free[DataOp, A]

  private def readFromKafka(topic: String, retries: Int): DataOpFree[Either[String, List[String]]] =
    Free.liftF(Retryable(ReadFromKafka(topic), retries))

  private def transformData(records: List[String]): DataOpFree[List[String]] =
    Free.liftF(TransformData(records))

  private def writeToDatabase(records: List[String], retries: Int): DataOpFree[Either[String, Unit]] =
    Free.liftF(Retryable(WriteToDatabase(records), retries))

  private def readAllFromDatabase(): DataOpFree[List[String]] =
    Free.liftF(ReadAllFromDatabase())

  private def logMessage(msg: String): DataOpFree[Unit] = Free.liftF(LogMessage(msg))

  def pipeline(topic: String): DataOpFree[Unit] = for
    _ <- logMessage(s"Starting processing for topic: $topic")
    readResult <- readFromKafka(topic, 3)
    - <- readResult match
      case Right(records) =>
        for
          _ <- logMessage(s"Read ${records.length} records:\n\t${records.mkString("\n\t")}")

          cleaned <- transformData(records)
          _ <- logMessage(s"Transformed ${cleaned.length} records")

          dbResult <- writeToDatabase(cleaned, 2)
          - <- dbResult match
            case Right(_) =>
              for
                _ <- logMessage("Write to DB successful")
                _ <- logMessage("Processing complete")
              yield ()
            case Left(err) =>
              for
                _ <- logMessage(s"Write to DB failed: $err")
                _ <- logMessage("Processing failed")
              yield ()
        yield ()
      case Left(err) =>
        for
          _ <- logMessage(s"Read from Kafka failed: $err")
          _ <- logMessage("Processing failed")
        yield ()

    _ <- readAllFromDatabase()
    _ <- logMessage("\n=============================\n")
  yield ()
