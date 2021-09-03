package com.karrar.fluxapplication;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@TestConfiguration
@Profile("test")
public class AmazonKinesisTestConfig {

    public static LocalStackContainer localStack;

    static {
        localStack = new LocalStackContainer().withServices(KINESIS);
        localStack.start();
    }

    @Bean
    public AmazonKinesis amazonS3() {
        return AmazonKinesisClientBuilder.standard()
            .withCredentials(localStack.getDefaultCredentialsProvider())
            .withEndpointConfiguration(localStack.getEndpointConfiguration(KINESIS))
            .build();
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        return localStack.getDefaultCredentialsProvider();
    }
}
