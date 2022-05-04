## installation and running

run ``shard`` to get depedencies

- run ``crystal src/blockchain.cr``
- go to ``http://localhost:3000``


## running with docker

running by
```
docker build -t image_name_choosing:latest .
```
```
docker container run -d -p 3000:3000 image_name_choosing
```
and then go to on ``localhost:3000``


clean up by 
- run ``docker container ls``
- copy container ID from running process
- run ``docker container rm -f <CONTAINER_ID>``
- run ``docker image rm image_name_choosing``
