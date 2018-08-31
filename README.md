# SimpleSyncedServers

Simple code for running two synced servers together.
The servers save the last POST request's content sent to either one of them, and it can be retrieved by GET to either one.
Config the hosts in the "config.properties" file, while in each server the "other" and "this" properties should be accordingly.

API:
- POST/GET to ports 80 or 8080, using the API "/exercise"
- example: curl -X POST -H "Content-Type: application/json" -d '{"something": "blabla"}' http://server1:8080/exercise

Dependencies were managed with maven, and compiled using "mvn clean compile assembly:single".
