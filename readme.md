# "Opening Hours" exercise

## 1. HTTP API

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

...which should return this:

````
Monday: Closed
Tuesday: 10 AM - 6 PM
Wednesday: Closed
Thursday: 10 AM - 6 PM
Friday: 10 AM - 1 AM
Saturday: 10 AM - 1 AM
Sunday: 12 PM - 9 PM
````

Make an invalid POST request that returns 400 Bad Request:

``curl http://localhost:8080/hours -H "Content-Type: application/json" --data @examples/brokeninput1.json``


## 2. Data format

In the current JSON format, opening times are subordinate to weekdays:

````
schedule -> weekday -> time
                       |-> secondOfDay
                       |-> type (open/close)
````

However, a weekday can have multiple opening periods (1-to-many), and an opening period can
span across weekdays (many-to-1), e.g. Fri 10 PM - (Sat) 3 AM.
Thus, opening periods/times are not logically subordinate to weekdays, which makes the given format somewhat awkward.

Furthermore, opening times and closing times being stored as separate datums gives rise to potentially
inconsistent data: opening times without a matching closing time, or vice versa.

One alternative data structure is one where opening and closing times are coupled
into intervals (as tuples), and these times include the data about which weekday the occur on.
For example, the start time and end time could be stored as *the time of week in seconds* (i.e. seconds after 12 AM Monday).

````
schedule -> interval ->
            |-> start
            |    |-> secondOfWeek
            |
            |-> end
                 |-> secondOfWeek
````

This is the structure used in the model of this code solution (com.mertant.openinghours.model.OpeningHours),
with the exception that the model stores the time of week as a tuple of *weekday and second-of-day* rather than as
*second-of-week*. This makes the times easier to read for humans.

Storing the time as *second-of-week*, on the other hand, might be more appropriate for reading by computers.
Storing the data in this format in a database would give us the following database schema:

````
                 1      *
Schedule(pk_id) ---------> Interval(pk_id, fk_schedule_id, start, end) 

````

A typical query to the opening hours database might presumably be: "Give me all the restaurants that are open on Thursday at 1 PM." 

Storing the data in the per-weekday format used in the current JSON could make it inefficient to make
such queries about opening times. Unless we trust our data to be consistent (i.e. each opening time has a closing time),
and we assume that no opening period spans more than two days (e.g. a restaurant can have an opening period
Wed-Thu but not Wed-Fri), then we start having to check not just the weekday in question, 
but check and compare times from different weekdays. Needing to constantly "break"
the weekday boundary like this indicates, again, a failure of the data structure.

In contrast, the database schema suggested above makes such queries easy: get all intervals that
start before now and end before now.

````
SELECT * FROM interval WHERE
    (start < secondsOfWeekNow() AND end > secondsOfWeekNow());

````

The performance of the query could potentially be increased by indexing the intervals by their start time.

Finally, when we serialize this format into JSON, we save space as we do not need to store the names of weekdays,
or the redundant information about the "type of time" (open/close).

In summary, the benefits of the proposed data structure:
- flat logical hierarchy of opening periods (intervals) matches the concept of linear time
- coupling of opening and closing time creates consistent data
- possible performance benefits for a typical query
- more compact

There are other possibilities, but this structure perhaps strikes a good balance between machine-optimized
and human-understandable data.

The proposed data structure presented as JSON:

````
{
    "intervals": [
        {
            "start": 118800,
            "end": 151200
        },
        {
            "start": 205200,
            "end": 237600
        }
    ] 
}
````

or, alternatively:

````
{
    "intervals": [
        {
            "start": {
                "day": 1,
                "seconds": 32400
            },
            end: {
                "day": 1,
                "seconds": 64800
             }
        },
        {
            "start": {
                "day": 2,
                "seconds": 32400
            },
            end: {
                "day": 2,
                "seconds": 64800
             }
        },
    ] 
}
````