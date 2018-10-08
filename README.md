# Kafka centralized logs
>POC to study how Kafka can be used to centralize logs

In progress..

## How-to run

In order to elasticsearch work you will need to run in your terminal:

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
  * Filebeat to Kafka
  
## Meta

Alex Rocha - [about.me](http://about.me/alex.rochas)
