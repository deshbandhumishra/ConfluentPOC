import java.io.FileReader
import java.util.{Collections, Properties}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import model.RecordJSON
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.DurationConverters.ScalaDurationOps
import scala.language.postfixOps

object Consumer extends App {
  val configFileName ="resources/ConfluentKafkaCloud.config" //args(0)
  val topicName = "topic-test1"
  val props = buildProperties(configFileName)
  val consumer = new KafkaConsumer[String, JsonNode](props)
  val MAPPER = new ObjectMapper
  consumer.subscribe(Collections.singletonList(topicName))
  var total_count = 0L
  while(true) {
    println("Polling")
    val records = consumer.poll((1 second).toJava)
    val recordJSON = records.asScala.toVector.map(_.value())
    for (record <- recordJSON) {

      val key = record.get(1)//)elements()//get(1)
      val value = record.get("id")
      val countRecord = MAPPER.treeToValue(value, classOf[RecordJSON])
      total_count = 1//countRecord.getid()
      println(s"Consumed record with key $key and value $value, and updated total count to $total_count")
    }
  }
  consumer.close()

  def buildProperties(configFileName: String): Properties = {
    val properties = new Properties()
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonDeserializer")
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "scala_example_group")
    properties.load(new FileReader(configFileName))
    properties
  }

}