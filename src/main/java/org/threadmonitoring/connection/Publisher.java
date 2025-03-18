/*
package org.threadmonitoring.connection;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Publisher {

    private static final String TOPIC = "map-list-data";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";


    public static void run() {
        Producer<String, byte[]> producer = createKafkaProducer();

        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(
                    Publisher.class.getClassLoader()
                            .getResourceAsStream("publisher_schema.avsc")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Integer> sampleMap = new HashMap<>();
        sampleMap.put("key1", 100);
        sampleMap.put("key2", 200);

        List<String> sampleList = Arrays.asList("one", "two", "three");

        GenericRecord record = new GenericData.Record(schema);
        record.put("sampleMap", sampleMap);
        record.put("sampleList", sampleList);

        byte[] serializedBytes = serializeToAvro(record, schema);

        ProducerRecord<String, byte[]> producerRecord =
                new ProducerRecord<>(TOPIC, "key-1", serializedBytes);

        producer.send(producerRecord, (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            } else {
                System.out.println("Wysłano rekord z offsetem: " + metadata.offset());
            }
        });

        producer.close();
    }

    // Funkcja serializująca rekord Avro do byte[]
    private static byte[] serializeToAvro(GenericRecord record, Schema schema) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        try {
            writer.write(record, encoder);
            encoder.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private static Producer<String, byte[]> createKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        // Optymalizacja wydajności
        props.put("acks", "1");
        props.put("compression.type", "snappy");
        props.put("batch.size", "16384");
        props.put("linger.ms", "5");
        props.put("buffer.memory", "33554432");

        return new KafkaProducer<>(props);
    }

}
*/
