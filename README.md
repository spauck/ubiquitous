# Ubiquitous

_A simple place to store and retrieve data using ubiquitous technologies_

## Principles & Goals

This project came about when I wanted to analyse some runtime data in a JVM app.
I didn't need to setup a metrics stack and didn't want to drag extra dependencies into the project.
**Ubiquitous** therefore aims to be simple way to stage some data using only the most readily accessible technologies.
It aims to avoid taking on dependencies itself and do as much as possible using only the standard JRE.

Ideally the interface should hardly need a specification and is mostly convention based.
As such, it is conceptually access to data (encoded as JSON) following REST principles (via HTTP).

## Example

It should therefore be possible to the following (and similar):

```bash
$ curl -X PUT -H "Content-Type: application/json" \
  -d '{ "message": "Hello", "recipient": "World" }' \
  http://localhost:9180/path/for/messages/

$ curl -X GET http://localhost:9180
{
  "path": {
    "for": {
      "messages": {
        "message": "Hello",
        "recipient": "World"
      }
    }
  }
}
```
