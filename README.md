# myRetail RESTful Service

The following is the tech stack that i used for this assignment


| Technology| Description|
| ---- |----|
| Scala          | Scala is a general-purpose programming language providing support for functional programming and a strong static type system|
| Akka         | a toolkit for building highly concurrent, distributed, and resilient message-driven applications for Java and Scala      |
| Akka http     | The Akka HTTP modules implement a full server- and client-side HTTP stack on top of akka-actor and akka-stream      |
| Cassandra   | NoSql Data store
| sbt pillar | Schema/Data Migrations
| Docker, Docker compose  | containers
| Sbt - Simple build tool  | Build automation
| Scala test and embedded cassandra | Unit testing
| SCoverage | Code coverage
| Gatling  | Load testing
| Swagger | API documentation
| IntelliJ IDEA  | IDE

## Testing the application

#### Pre-requisites
Install sbt - On a Mac `brew install sbt@1`

Install Docker 

To run the application clone the repository and from the root of the repository run
`sbt demo` 

Swagger URL will be available at `http://localhost:8080/myRetail/swagger
`

##### POST - Insert a product information with the following json input into the NoSql datastore
```json
{"id":13860428,"current_price":{"value":13.49,"currency_code":"USD"}}
```
or post a message using ```curl -X POST "http://localhost:8080/products" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"id\":13860428,\"current_price\":{\"value\":13.49,\"currency_code\":\"USD\"}}"```


##### GET - Get the product information
Provide the id as "13860428" and execute the GET from swagger. This should return 200 OK and the below response body
```json
{
  "id": 13860428,
  "name": "The Big Lebowski (Blu-ray)",
  "current_price": {
    "value": 13.49,
    "currency_code": "USD"
  }
}
```

##### PUT - Update the product price
Provide the id as "13860428" and the following json body
```json
{
  "id": 13860428,
  "name": "The Big Lebowski (Blu-ray)",
  "current_price": {
    "value": 15.00,
    "currency_code": "USD"
  }
}
```
This should respond with a 200 OK and when you get the product information this should return the product information with the updated price

### Unit tests and code coverage report

run `sbt coverage test coverageReport`

### Load tests
run `sbt gatling:test`




