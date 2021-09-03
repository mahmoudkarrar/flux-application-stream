# flux-application-stream

microservice which exposes 1 endpoint that accepts id and endpoint query params.

#### dependency
* java 11
* Spring Boot 2.5.4
* spring-cloud-starter-aws 2.2.6

#### what it does
  1. It accepts `GET` requests, then emits each request's  into a `reactor.core.publisher.Sinks` publisher
  for later processing and in case endpoint query param is present it fires a post request to it including the current requests count
  , each minute it counts all distinct published ids to AWS Kinesis Data stream.

#### Implementation Decisions

  1. It was required that the endpoint can process 10K requests per second this was a clear indication for me to favor webflux and netty
   over the normal servlet web stack. [reference](https://filia-aleks.medium.com/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0)
  2. AWS Kinesis it's easy to push data to and very scalable. 

#### How it works    
1. AWS Kinesis
    Assumption that this service runs in a machine with contains `.aws` folder with valid aws access credentials.
    
    Kinesis Stream is made optional you can toggle it on or off based on the configuration can be found in `application.yml`
    it's set to true by default, if you wish not to push data to kinesis you might either remove the config or set it to false. 
    
2. Performance Test

    I used apache benchmark to get some figures the results were as follows
    
    ```
   Server Software:        
   Server Hostname:        localhost
   Server Port:            8080
   
   Document Path:          /api/smaato/accept?id=ddsa
   Document Length:        2 bytes
   
   Concurrency Level:      100
   Time taken for tests:   1.874 seconds
   Complete requests:      10000
   Failed requests:        0
   Keep-Alive requests:    0
   Total transferred:      800000 bytes
   HTML transferred:       20000 bytes
   Requests per second:    5336.02 [#/sec] (mean)
   Time per request:       18.741 [ms] (mean)
   Time per request:       0.187 [ms] (mean, across all concurrent requests)
   Transfer rate:          416.88 [Kbytes/sec] received
   
   Connection Times (ms)
                 min  mean[+/-sd] median   max
   Connect:        0    1   7.8      1     347
   Processing:     2   16  36.7      9     356
   Waiting:        2   15  35.7      9     356
   Total:          5   17  37.3     10     357
   
   Percentage of the requests served within a certain time (ms)
     50%     10
     66%     14
     75%     15
     80%     17
     90%     21
     95%     27
     98%    102
     99%    181
    100%    357 (longest request)
   ```
    Results may vary depends on machine's processor mine are `2,4 GHz 8-Core Intel Core i9`, I have also created the 
    other webservice which processes the post requests on the same machine.
    
   
#### Running app with docker
   Simply run `./start.sh` from your terminal make sure that you have `mvn` and `docker` installed
   
#### Running app with maven
   run `mvn clean install`
