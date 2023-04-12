# Detached entity exception with Hibernate

## Introduction 

One of the problem that we face when working with Hibernate is the typical error: 
org.hibernate.PersistentObjectException` to JPA `PersistenceException` : detached entity passed to persist

In this article I'm going to show some ways to solve this problem when is happening.
https://refactorizando.com/error-hibernate-detached-entity-passed-to-persist

## Example

We have some test to check the org.hibernate.PersistentObjectException error, just run it to check it.

### How does it run?

    mvn test


