#!/bin/bash

# Backup Database
cd /home/hajiang/enc-selector/sql
mysqldump -u encsel -pencsel encsel > backup_`date +%Y%m%d`.sql

# Delete files older than 7 days
find . -name backup_*.sql -mtime +7 -delete

# Upload to GIT
git add -A
git commit -m "Daily DB Backup"
git push origin master

