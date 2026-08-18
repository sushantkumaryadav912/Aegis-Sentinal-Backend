-- Aegis Sentinel IAM Schema

-- 1. Organizations
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Workspaces (Tenant Isolation)
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_org_workspace_slug UNIQUE (organization_id, slug)
);

-- 3. Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Roles
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_system_role BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Permissions
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL
);

-- Role-Permission Junction
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 6. User Roles (RBAC with Organization/Workspace Scope)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role_scope UNIQUE (user_id, role_id, organization_id, workspace_id)
);

-- 7. Sessions (Refresh Token Storage & Revocation)
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Query Optimization
CREATE INDEX idx_users_org ON users(organization_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_workspaces_org ON workspaces(organization_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_org_ws ON user_roles(organization_id, workspace_id);
CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_token_hash ON sessions(refresh_token_hash);

-- Seed System Roles
INSERT INTO roles (id, name, description, is_system_role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'PLATFORM_ADMIN', 'Super administrator with platform-wide access', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'ORG_ADMIN', 'Organization administrator with full tenant access', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'SECURITY_ANALYST', 'Security analyst with incident and workflow access', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'AUDITOR', 'Read-only access for compliance and auditing', TRUE);

-- Seed Core Permissions
INSERT INTO permissions (id, name, description, category) VALUES
    ('10000000-0000-0000-0000-000000000001', 'org:manage', 'Manage organization settings and billing', 'ORGANIZATION'),
    ('10000000-0000-0000-0000-000000000002', 'workspace:manage', 'Create and manage workspaces', 'WORKSPACE'),
    ('10000000-0000-0000-0000-000000000003', 'user:manage', 'Manage users and role assignments', 'USER'),
    ('10000000-0000-0000-0000-000000000004', 'alert:read', 'View security alerts and incidents', 'INCIDENT'),
    ('10000000-0000-0000-0000-000000000005', 'alert:write', 'Create, update, and resolve alerts', 'INCIDENT'),
    ('10000000-0000-0000-0000-000000000006', 'workflow:execute', 'Execute response workflows', 'SOAR'),
    ('10000000-0000-0000-0000-000000000007', 'audit:read', 'View audit logs', 'AUDIT');

-- Assign Permissions to System Roles
-- ORG_ADMIN gets all org/workspace/user/alert/workflow/audit permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000006'),
    ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000007');

-- SECURITY_ANALYST gets alert:read, alert:write, workflow:execute
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004'),
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006');

-- AUDITOR gets alert:read, audit:read
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004'),
    ('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000007');
