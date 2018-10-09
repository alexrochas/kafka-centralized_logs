# Kafka centralized logs
>POC to study how Kafka can be used to centralize logs

ELK stack is something common nowadays, but recently I have the need
to emit events in an very coupled architecture in order to be reactive to
problems and troubleshooting. 

Just with the ELK stack this wans't possible (my best approach was an query into
elasticsearch, some filters and them send to Slack).

Already headed about using Kafka with the ELK stack to have centralized logs and
here is how to do it.

## What it is?

### App

My app is completely dumb, all he does is print "Log this if you dare!"
in a loop with 5 seconds interval.

```docker
#Dockerfile_app
FROM gradle:alpine

USER root

WORKDIR /usr/src/app

COPY ./dummy-logger /usr/src/app

CMD ./gradlew bootRun
```

Just **UP** and running.

### Filebeat

Filebeat in a real architecture (insert your cloud infraestructure here) will
be deployed with your containerized app and scan the Docker folder logs for
any new log file. With that (and some configuration) it will send data to
Kafka (a dynamic created topic).

```yml
#filebeat.yml

filebeat.prospectors:
- type: log
  json.keys_under_root: true
  json.message_key: log
  enabled: true
  encoding: utf-8
  document_type: docker
  paths:
# Location of all our Docker log files (mapped volume in docker-compose.yml)
    - '/usr/share/filebeat/dockerlogs/data/*/*.log'
processors:
# decode the log field (sub JSON document) if JSONencoded, then maps it's fields to elasticsearch fields
- decode_json_fields:
    fields: ["log"]
    target: ""
# overwrite existing target elasticsearch fields while decoding json fields
    overwrite_keys: true
- add_docker_metadata: ~

filebeat.config.modules:
  path: ${path.config}/modules.d/*.yml
  reload.enabled: false

setup.template.settings:
  index.number_of_shards: 3
  
output.kafka:
  # initial brokers for reading cluster metadata
  hosts: ["localhost:9092"]
  # message topic selection + partitioning
  topic: '%{[log_topic]:dummy}-log'
  partition.round_robin:
    reachable_only: false
  required_acks: 1
  compression: gzip
  max_message_bytes: 1000000
  
logging.to_files: true
logging.to_syslog: false
```

### Kafka

Here is the star and all he does is what we expect from him. :heart:

> Now we are in Kafka, how do we go back to elasticsearch?

### Logstash

Logstash here will be listening to Kafka and writing to Elasticsearch.
The nice part about this is that he accept a lot of configurations and
in my example, it is scanning a patter base topic (read: regex).

```
#logstash-config/pipeline/logstash-kafka.conf
input {
    kafka {
            bootstrap_servers => "localhost:9092"
            topics_pattern => [".*"]
    }
}

output {
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "logstash"
        document_type => "logs"
    }
    stdout { codec => rubydebug }
}
```

Receive from a Kafka topic ".\*" (also know as "anything") and send to Elasticsearch.

## Elasticsearch

Like Kafka, still do what we think it should do. Important here is that
Logstash must create the Index automatically, if it doesn't, something is wrong.

## Kibana

Use it but don't depend on it. Please.

## How to run

The main content here is probably the order you should start the containers.

```bash
~/[PROJECT_HOME]$ docker-compose -f docker-compose-app-filebeat.yml up
~/[PROJECT_HOME]$ docker-compose -f docker-compose-kafka.yml up
~/[PROJECT_HOME]$ docker-compose -f docker-compose-logstash.yml up
~/[PROJECT_HOME]$ docker-compose -f docker-compose-elasticsearch.yml up
```

## Troubleshooting (must be updated)

In order to elasticsearch work (local) you will need to run in your terminal:

```shell
~$ sudo sysctl -w vm.max_map_count=262144
```

Also, elasticsearch will keep thorwing an warning, something like 
`[2018-10-08T18:54:15,980][INFO ][o.e.c.r.a.DiskThresholdMonitor] [CkBiNBo] low disk watermark [85%] exceeded on [CkBiNBoMQyuW7HMb2f7gIg][CkBiNBo][/usr/share/elasticsearch/data/nodes/0] free: 6.7gb[12.1%], replicas will not be assigned to this node`.
To get read of that, do a PUT like bellow:

```json
[PUT] http://localhost:9200/_cluster/settings
{
    "transient" : {
        "cluster.routing.allocation.disk.threshold_enabled" : false
    }
}
```

## Roadmap

  * ~~Basic usage working~~
  * ~~Filebeat to Kafka~~
  * Add template json from logstash to elasticsearch
  * Add image to explain the interactions between services
  * ?
  
## Meta

Alex Rocha - [about.me](http://about.me/alex.rochas)
