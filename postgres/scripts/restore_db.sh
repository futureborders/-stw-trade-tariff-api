#!/bin/bash
set -e
psql -f /data/create_signposting_schema.sql signposting -U signposting_user
