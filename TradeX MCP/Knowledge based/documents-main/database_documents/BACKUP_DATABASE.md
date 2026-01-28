# Backup Database
## Backup all databases
1. ssh to the database server
server ip: 172.33.10.65
server ssh username: ubuntu
2. backup all database schema
```
mysqldump -u <database_username> -p --all-databases > all_databases_backup.sql
```

## Restore the database
```
mysql -u <database_username> -p < all_databases_backup.sql
```
