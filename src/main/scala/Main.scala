import interpreter.Interpreter
import monad.given
import pipeline.DataPipeline.pipeline

@main def main(): Unit =
  for i <- 1 to 5 do
    pipeline("events-topic").foldMap(Interpreter)
