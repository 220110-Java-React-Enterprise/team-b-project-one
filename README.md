# **Project 1:**
By: David Alvarado, Maja Wirkijowska

## Description

## Tech Stack:
  - HTTP
  - ORM (custom made for data persistance)
  - Servlets
  - Java 8
  - AWS
  - Apache Maven for dependencies and project management
  - Git & Github for version control
  - MariaDB RDS for data persistance
  - Tomcat server 
  - Postman

## Project 1

### Part 1: Custom ORM

### Part 2: Web App
Using Postman as the front-end interface, the ticket kiosk is remotely available on AWS and uses the Part 1: Customer ORM as a dependecy to create tables and objects on MariaDB. Java Servlets allow the web-app to store, manipulate, and retrieve objects in response to HTTP requests. 

## Minimum Requiremnets:
  1. Proper use of OOP principles
  2. CRUD operations are supported for at least 2 types of objects.
  3. Communication is done with HTTP exchanges, and resources are transmitted as JSON in request/response bodies.
  4. JDBC and persistence logic should all be part of your ORM which abstracts this away from the rest of the application.
  4. Documentation (all classes and methods have adequate Javadoc comments)
  5. All Exceptions are caught and logged to a file

## Bonus Features
  1. ORM can build foreign key relations according o object references.
  2. ORM can design schema on the fly

## User Stories
  -As a user, I store JSON objects by invoking the proper endpoint (POST/Create).
  -As a user, I can change objects by invoking the proper endpoint (PUT/Update).
  -As a user, I can retrieve objects by invoking the proper endpoint (GET/Read).
  -As a user, I can delete objects by invoking the proper endpoint (DELETE/Delete).
  -As a user, I can retrieve all objects that belong to me. (transmit the user as part of the request header, and build a relation in the    database in some way to tie the objects to the user)
