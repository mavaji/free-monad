import interpreter.{Database, Interpreter, InterpreterWithError}
import monad.given
import pipeline.DataPipeline.processPipeline

@main def main(): Unit =
  processPipeline("events-topic").foldMap(Interpreter)
  println("\nDatabase contents:\n\t" + Database.storage.toList.mkString("\n\t"))

  println("=============================")

  processPipeline("events-topic").foldMap(InterpreterWithError)
  println("\nDatabase contents:\n\t" + Database.storage.toList.mkString("\n\t"))

  println("=============================")

  processPipeline("events-topic").foldMap(Interpreter)
  println("\nDatabase contents:\n\t" + Database.storage.toList.mkString("\n\t"))
