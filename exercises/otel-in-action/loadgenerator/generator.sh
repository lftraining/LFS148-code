#!/bin/sh
while true; do \
    curl -v http://todoui-thymeleaf:8090 --data toDo=Sample; \
    sleep 5; curl --silent --output /dev/null http://todoui-thymeleaf:8090; \
    sleep 5; curl http://todoui-thymeleaf:8090/Sample --data value='Done!'; \
    sleep 5; curl -v http://todoui-flask:5000/add --data todo=Sample; \
    sleep 5; curl --silent --output /dev/null http://todoui-flask:5000; \
    sleep 5; curl -v http://todoui-flask:5000/delete --data todo=Sample; \
done
