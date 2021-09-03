package com.karrar.fluxapplication.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class KinesisService {

    private final AWSCredentialsProvider awsCredentialsProvider;

    @Value("${kinesis.stream.name:Requests-Count}")
    String streamName;

    @Value("${kinesis.stream.enabled:false}")
    Boolean streamEnabled;

    @Autowired
    public KinesisService(AWSCredentialsProvider awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    public void streamData(Long requestsCount) {

        ofNullable(streamEnabled)
                .filter(Boolean::booleanValue)
                .ifPresent(b -> doStreamData(requestsCount));

    }

    private void doStreamData(Long requestsCount) {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();

        log.info("Trying to stream data to AmazonKinesis request count {}",requestsCount );
        clientBuilder.setRegion("eu-central-1");
        clientBuilder.setCredentials(awsCredentialsProvider);
        clientBuilder.setClientConfiguration(new ClientConfiguration());

        AmazonKinesis kinesisClient = clientBuilder.build();

        PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
        putRecordsRequest.setStreamName(streamName);

        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(requestsCount).getBytes()));
        putRecordsRequestEntry.setPartitionKey("partitionKey-1");

        putRecordsRequest.setRecords(List.of(putRecordsRequestEntry));
        PutRecordsResult putRecordsResult = kinesisClient.putRecords(putRecordsRequest);
        log.info("Put in Kinesis Result {}", putRecordsResult);
    }
}
