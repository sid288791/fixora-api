-- Bootstrap a separate database/user for GoAlert.
-- Runs before init.sql (docker-entrypoint-initdb.d scripts execute in
-- alphabetical order) so GoAlert has a database ready on first boot.
CREATE USER goalert_user WITH PASSWORD 'goalert_password';
CREATE DATABASE goalert_db OWNER goalert_user;
GRANT ALL PRIVILEGES ON DATABASE goalert_db TO goalert_user;
