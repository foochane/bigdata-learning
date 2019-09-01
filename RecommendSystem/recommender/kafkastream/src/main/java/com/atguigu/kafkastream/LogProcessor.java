/*
 * Copyright (c) 2017. WuYufei All rights reserved.
 */

package com.atguigu.kafkastream;

import com.atguigu.commons.constant.Constants;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * Created by wuyufei on 2017/6/18.
 */
public class LogProcessor implements Processor<byte[], byte[]> {
    private ProcessorContext context;

    public void init(ProcessorContext context) {
        this.context = context;
    }

    public void process(byte[] dummy, byte[] line) {
        String input = new String(line);
        if (input.contains(Constants.USER_RATING_LOG_PREFIX())) {
            input = input.split(Constants.USER_RATING_LOG_PREFIX() + ":")[1].trim();
            context.forward("logProcessor".getBytes(), input.getBytes());
        }
    }

    public void punctuate(long timestamp) {
    }

    public void close() {
    }
}
