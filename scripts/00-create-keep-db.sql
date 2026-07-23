-- Bootstrap a separate database/user for the Keep platform.
-- This runs before init.sql (docker-entrypoint-initdb.d scripts execute in
-- alphabetical order) so the Keep backend has a database ready on first boot.
CREATE USER keep_user WITH PASSWORD 'keep_password';
CREATE DATABASE keep_db OWNER keep_user;
GRANT ALL PRIVILEGES ON DATABASE keep_db TO keep_user;
