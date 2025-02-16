```plantuml
@startuml
!theme aws-orange
skinparam defaultFontName "Gabriele Black Ribbon FG"
title foldMap: Natural Transformation via Interpreter transforming DSL into IO operations

' DSL pipeline created by flatMap and liftF
package "DSL Pipeline (Free Monad)" as dsl {
  rectangle "ReadFromKafka(topic)" as readFromKafka
  rectangle "LogMessage('Read...')" as logMessage
  rectangle "TransformData(records)" as transformData
  rectangle "LogMessage('Transformed...')" as logMessage2
  rectangle "WriteToDatabase(records)" as writeToDatabase
}

' Target pipeline (IO)
package "Target Pipeline (IO)"  as io{
  rectangle "Kafka.read(topic)" as targetReadFromKafka
  rectangle "println('Read...')" as targetLogMessage
  rectangle "Processor.transform(records)" as targetTransformData
  rectangle "println('Transformed...')" as targetLogMessage2
  rectangle "Database.write(records)" as targetWriteToDatabase
}


' NT transforms DSL pipeline into Target pipeline via Interpreter
readFromKafka   -r->   targetReadFromKafka : "                Natural transformation                 "
logMessage      -r->   targetLogMessage : "              Natural transformation                  "
transformData   -r->   targetTransformData : "            Natural transformation               "
logMessage2     -r->   targetLogMessage2 : "            Natural transformation             "
writeToDatabase -r->   targetWriteToDatabase : "            Natural transformation               "

targetReadFromKafka -d[#lightblue]-> targetLogMessage : "flatMap"
targetLogMessage -d[#lightblue]-> targetTransformData : "flatMap"
targetTransformData -d[#lightblue]-> targetLogMessage2 : "flatMap"
targetLogMessage2 -d[#lightblue]-> targetWriteToDatabase : "flatMap"


' FlatMap chain in the DSL pipeline
readFromKafka -d[#lightblue]-> logMessage : "flatMap"
logMessage -d[#lightblue]-> transformData : "flatMap"
transformData -d[#lightblue]-> logMessage2 : "flatMap"
logMessage2 -d[#lightblue]-> writeToDatabase : "flatMap"

@enduml
```