FROM mysql:5.7
ADD ./schema.sql /docker-entrypoint-initdb.d/databaseSetup01.sql
ADD ./data.sql /docker-entrypoint-initdb.d/databaseSetup02.sql
