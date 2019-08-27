/*
 * Copyright (c) 2017. WuYufei All rights reserved.
 */

package com.atguigu.kafkastream;

import com.atguigu.commons.conf.ConfigurationManager;
import com.atguigu.commons.constant.Constants;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;

import java.util.Properties;

/**
 * Created by wuyufei on 2017/6/18.
 */
public class Application {

    public static void main(String[] args) {

        String brokers = ConfigurationManager.config().getString(Constants.KAFKA_BROKERS());
        String zookeepers = ConfigurationManager.config().getString(Constants.KAFKA_ZOOKEEPER());
        String from = ConfigurationManager.config().getString(Constants.KAFKA_FROM_TOPIC());
        String to = ConfigurationManager.config().getString(Constants.KAFKA_TO_TOPIC());

        if (args.length != 4) {
            System.out.println("Usage: kafkaStream <brokers> <zookeepers> <from> <to>\n" +
                    "  <brokers> is a list of one or more Kafka brokers\n" +
                    "  <zookeepers> is a list of one or more Zookeeper nodes\n" +
                    "  <from> is a topic to consume from\n" +
                    "  <to> is a topic to product to\n" +
                    "  =================================\n" +
                    "  Using Default Value\n");
        } else {
            brokers = args[0];
            zookeepers = args[1];
            from = args[2];
            to = args[3];
        }

        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "logFilter");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zookeepers);

        StreamsConfig config = new StreamsConfig(settings);

        TopologyBuilder builder = new TopologyBuilder();

        builder.addSource("SOURCE", from)
                .addProcessor("PROCESS", () -> new LogProcessor(), "SOURCE")
                .addSink("SINK", to, "PROCESS");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
    }
}
