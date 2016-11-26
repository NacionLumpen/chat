[back to main readme](/README.md)

# "Dynamic" actor model with Scala/Akka

We can consider this a dynamic implementation because Akka actors have
changing behaviors of type `Any => Unit`, effectively discarding Scala
static type safety.

## Dependencies

A working [SBT][sbt] installation.

[sbt]: http://www.scala-sbt.org/

## Launching the server

From SBT:

    > sbt run
   
Creating a standalone JAR:

    > sbt assembly
    > java -jar target/scala-2.11/akkachat.jar
   
## Connecting to the server

No client at the moment but you can use the good old `telnet`.

    > telnet localhost 2121
    NICK myself
    200 renamed to myself
    ...
