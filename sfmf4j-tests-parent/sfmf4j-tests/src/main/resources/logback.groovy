import static ch.qos.logback.classic.Level.*
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %p - %C{1} %L - %msg%n"
  }
}
root(WARN, ["STDOUT"])
logger("sfmf4j.osgi.test", TRACE)