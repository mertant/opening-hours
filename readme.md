# "Opening Hours" task

## 1. Coding task: HTTP API

### Tech used

Project seed: "Akka HTTP Quickstart Scala" downloaded from Lightbend: https://developer.lightbend.com/start/?group=akka&project=akka-http-quickstart-scala

Build tool & dependency management: SBT

HTTP library: Akka HTTP

JSON library: spray-json (via akka-http-spray-json)

### Building

Build:

``sbt compile``

Run tests:

``sbt test``

Start server on port 8080:

``sbt run``

Confirm that the server is running:

``curl http://localhost:8080/hours``

Make a POST request:

``curl http://localhost:8080/hours -H "Content-Type: application/json" --data @examples/exampleinput1.json``

which should return this:

````
Monday: Closed
Tuesday: 10 AM - 6 PM
Wednesday: Closed
Thursday: 10 AM - 6 PM
Friday: 10 AM - 1 AM
Saturday: 10 AM - 1 AM
Sunday: 12 PM - 9 PM
````

Make an invalid POST request that returns 500 Internal Server Error:

``curl http://localhost:8080/hours -H "Content-Type: application/json" --data @examples/brokeninput1.json``


### TODO

- create zip & test usage guide


## 2. Data format