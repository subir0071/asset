# Spring Boot Using Spring Data MongoDB Example

This project depicts the Spring Boot Example with Spring Data MongoDB and REST Example.

oc policy add-role-to-user system:image-puller system:serviceaccount:spring-sample-app-test:default -n spring-sample-app-dev
oc policy add-role-to-user system:image-puller system:serviceaccount:spring-sample-app-prod:default -n spring-sample-app-dev

oc policy add-role-to-user edit system:serviceaccount:spring-sample-app-cicd:jenkins -n spring-sample-app-dev
oc policy add-role-to-user edit system:serviceaccount:spring-sample-app-cicd:jenkins -n spring-sample-app-test
oc policy add-role-to-user edit system:serviceaccount:spring-sample-app-cicd:jenkins -n spring-sample-app-prod

oc new-app https://github.com/sourabhgupta385/spring-boot-mongodb-example.git --strategy=pipeline --name=spring-pipeline -n spring-sample-app-cicd

curl http://localhost:8095/rest/users/all

mongodb://${db:orders-db}:27017/data

sonar token  f14721beb9ca305ab556cece40954d48bf35de25
