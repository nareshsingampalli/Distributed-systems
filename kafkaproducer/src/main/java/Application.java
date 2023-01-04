import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class Application {
	private static final String TOPIC = "events";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093,localhost:9094";
	public static void main(String[] args) {
		Producer<Long, String> kafkaProducer = createProducer(BOOTSTRAP_SERVERS);
		
		try {
			produceMessages(10, kafkaProducer);
		}catch(InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}finally {
			kafkaProducer.flush();
			kafkaProducer.close();
		}
	}
	
	public static void produceMessages(int numberOfMessages, Producer<Long, String> kafkaProducer) throws InterruptedException, ExecutionException {
//		int partition = 3;
		
		for(int i = 0; i < numberOfMessages; i++) {
			long key = i;
			String value = String.format("event %d",i);
			
			long timeStamp = System.currentTimeMillis();
			
			ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, key, value);
			
			RecordMetadata recordMetadata = kafkaProducer.send(record).get();
			
			System.out.println(String.format("Record with (key : %s, value: %s) was sent to (partition: %d , offset: %d)", record.key(), record.value(), recordMetadata.partition(), recordMetadata.offset()));
			
		}
	}
	
	public static Producer<Long, String> createProducer(String bootstrapServers){
		Properties properties = new Properties();
		
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "events-producer");
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		return new KafkaProducer<Long, String>(properties);
	}

}
