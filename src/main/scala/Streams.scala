import java.io.FileReader
import java.util.{Collections, Properties}

import model.RecordJSON

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._

import scala.util.Random

object Streams extends App {

  import Serdes._

  val configFileName ="resources/ConfluentKafkaCloud.config" //args(0)
  val topicName = "topic-test1"
  val props = buildProperties(configFileName)

  implicit val recordJSONSerde: Serde[RecordJSON] = getJsonSerde
  val builder: StreamsBuilder = new StreamsBuilder
  val records: KStream[String, RecordJSON] = builder.stream[String, RecordJSON](topicName)
  val counts: KStream[String, Long] = records.map((k, v) => (k, v.getid()))
  counts.print(Printed.toSysOut[String, Long].withLabel("Consumed record"))

  // Aggregate values by key
  val countAgg: KStream[String, Long] = counts.groupByKey
    .reduce((aggValue, newValue) => aggValue + newValue)
    .toStream

  countAgg.print(Printed.toSysOut[String, Long].withLabel("Running count"))

  val streams = new KafkaStreams(builder.build, props)
  streams.start()

  // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() { override def run { streams.close }}))

  def buildProperties(configFileName: String): Properties = {
    import org.apache.kafka.clients.consumer.ConsumerConfig
    import org.apache.kafka.streams.StreamsConfig
    val properties = new Properties()
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "java_streams_example_group" + Random.nextInt)
    // Disable caching to print the aggregation value after each record
    properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    properties.load(new FileReader(configFileName))
    properties
  }

  private def getJsonSerde(): Serde[RecordJSON] = {
    import io.confluent.kafka.serializers.{KafkaJsonDeserializer, KafkaJsonSerializer}
    import org.apache.kafka.common.serialization.{Serializer,Deserializer}
    val serdeProps: java.util.Map[String, Object] = Collections.singletonMap("json.value.type", classOf[RecordJSON])
    val mySerializer: Serializer[RecordJSON] = new KafkaJsonSerializer[RecordJSON]()
    mySerializer.configure(serdeProps, false)
    val myDeserializer: Deserializer[RecordJSON] = new KafkaJsonDeserializer[RecordJSON]()
    myDeserializer.configure(serdeProps, false)
    Serdes.fromFn(
      (topic, data) => mySerializer.serialize(topic, data),
      (topic, bytes) => Option(myDeserializer.deserialize(topic, bytes))
    )
  }

}