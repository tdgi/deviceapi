# Prerequisites:

## PostgreSQL:

Initialize a database server:

`initdb.exe -U postgres -E UTF8 -D "C:\Users\user\deviceapidb"`

Edit running port, for example 5434, in `postgresql.conf`

Start database:

`pg_ctl start -D C:\Users\user\deviceapidb`

Login via interactive terminal:

`psql -U postgres -p 5434`

Create new database:

`CREATE DATABASE deviceapi;`

Quit terminal:

`\q`

Use sample db data from `src/main/resources/dump-deviceapi-202411240839`. Restore it with a Database client, for example `DBeaver`

# Run Java App

Get the JAR file from https://github.com/tdgi/deviceapi/releases/tag/v1.0.0 and run:

`java -jar deviceapi.jar --dbhost=localhost --dbport=5434 --dbuser=postgres --dbcatalog=deviceapi`

The application listens for incoming HTTP requests on port 8080. 

## API endpoints:

* Retrieve all devices:

`GET http://localhost:8080/api/v1/devices`

* Retrieve a specific device:

`GET http://localhost:8080/api/v1/devices/{mac_address}`

* Register new device:

`POST http://localhost:8080/api/v1/devices`

with body:

`{
"mac": "aabbccddeeff",
"deviceType": "ACCESS_POINT"
}`

field `uplinkMac` is optional

* Retrieve all devices topology:

`GET http://localhost:8080/api/v1/devices/topology`

* Retrieve devices topology from a specific device:

`GET http://localhost:8080/api/v1/devices/topology/{mac_address}}`