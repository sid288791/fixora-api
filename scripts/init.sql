-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    ad_group VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create applications table
CREATE TABLE IF NOT EXISTS applications (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL UNIQUE,
    owner_email VARCHAR(255) NOT NULL,
    ad_group_mapping VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_applications_alias ON applications(alias);
CREATE INDEX idx_applications_status ON applications(status);

-- Sample data
INSERT INTO users (name, email, ad_group) VALUES
    ('Admin User', 'admin@example.com', 'admin-group'),
    ('User One', 'user1@example.com', 'dev-team');

INSERT INTO applications (name, alias, owner_email, ad_group_mapping, description, status) VALUES
    ('Sample App', 'sample-app', 'admin@example.com', 'dev-team,qa-team', 'Sample application for testing', 'ACTIVE');
