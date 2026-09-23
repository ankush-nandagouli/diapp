-- =========================================================================
-- DAKSHYAM INNOVATIONS - SUPABASE DATABASE SCHEMA
-- Execute this script in your Supabase Project SQL Editor (https://uttffudevdijhrpmewqx.supabase.co)
-- =========================================================================

-- 1. Company Profile
CREATE TABLE IF NOT EXISTS company_profile (
    id BIGINT PRIMARY KEY DEFAULT 1,
    company_name TEXT NOT NULL DEFAULT 'Dakshyam Innovations',
    tagline TEXT DEFAULT 'Engineering Next-Gen Robotics & Intelligent Systems',
    logo_url TEXT DEFAULT '',
    registration_number TEXT DEFAULT 'U72900MH2024PTC123456',
    gstin TEXT DEFAULT '27AABCD1234E1Z5',
    official_email TEXT DEFAULT 'contact@dakshyam.com',
    official_phone TEXT DEFAULT '+91 98765 43210',
    office_address TEXT DEFAULT 'Technology Incubation Park, Suite 402, India',
    website TEXT DEFAULT 'https://dakshyam.com',
    updated_by_partner TEXT DEFAULT 'Ankush Nandagouli',
    updated_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 2. Partners
CREATE TABLE IF NOT EXISTS partners (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    role TEXT DEFAULT 'Partner',
    email TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    capital_contributed DOUBLE PRECISION DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    avatar_url TEXT DEFAULT '',
    bio TEXT DEFAULT '',
    password TEXT DEFAULT '427752',
    must_change_password BOOLEAN DEFAULT TRUE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 3. Projects
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT PRIMARY KEY,
    title TEXT NOT NULL,
    client TEXT DEFAULT 'Internal R&D',
    status TEXT DEFAULT 'In Progress',
    budget DOUBLE PRECISION DEFAULT 0.0,
    assigned_partners TEXT DEFAULT '',
    image_url TEXT DEFAULT '',
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 4. Project Components (Bill of Materials)
CREATE TABLE IF NOT EXISTS project_components (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name TEXT NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price DOUBLE PRECISION DEFAULT 0.0,
    total_price DOUBLE PRECISION DEFAULT 0.0
);

-- 5. Daily Reports (DPR)
CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGINT PRIMARY KEY,
    project_id BIGINT DEFAULT 0,
    project_title TEXT DEFAULT '',
    reported_by_partner TEXT NOT NULL,
    summary TEXT NOT NULL,
    media_uri TEXT DEFAULT '',
    media_type TEXT DEFAULT 'NONE',
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 6. Cash Flow & Treasury Transactions
CREATE TABLE IF NOT EXISTS cash_flow (
    id BIGINT PRIMARY KEY,
    type TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    category TEXT DEFAULT 'General',
    description TEXT NOT NULL,
    paid_by_partner_name TEXT NOT NULL,
    paid_by_partner_id BIGINT DEFAULT 0,
    project_name TEXT DEFAULT '',
    receipt_uri TEXT DEFAULT '',
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 7. Activity & Business Alerts
CREATE TABLE IF NOT EXISTS activity_alerts (
    id BIGINT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT DEFAULT 'GENERAL',
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_read BOOLEAN DEFAULT FALSE
);

-- 8. Governance Removal Motions
CREATE TABLE IF NOT EXISTS removal_motions (
    id TEXT PRIMARY KEY,
    target_partner_id BIGINT NOT NULL,
    target_partner_name TEXT NOT NULL,
    proposed_by_partner_id BIGINT NOT NULL,
    proposed_by_partner_name TEXT NOT NULL,
    reason TEXT NOT NULL,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    votes TEXT DEFAULT '{}',
    status TEXT DEFAULT 'ACTIVE',
    eligible_partners_count INTEGER DEFAULT 4,
    required_majority INTEGER DEFAULT 3
);

-- 9. System Diagnostics & Audit Logs
CREATE TABLE IF NOT EXISTS system_logs (
    id TEXT PRIMARY KEY,
    level TEXT DEFAULT 'INFO',
    category TEXT DEFAULT 'SYSTEM',
    message TEXT NOT NULL,
    details TEXT DEFAULT '',
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- =========================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Grant full read/write access to anon/publishable key
-- =========================================================================

ALTER TABLE company_profile ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on company_profile" ON company_profile;
CREATE POLICY "Allow anon all on company_profile" ON company_profile FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE partners ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on partners" ON partners;
CREATE POLICY "Allow anon all on partners" ON partners FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on projects" ON projects;
CREATE POLICY "Allow anon all on projects" ON projects FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE project_components ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on project_components" ON project_components;
CREATE POLICY "Allow anon all on project_components" ON project_components FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE daily_reports ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on daily_reports" ON daily_reports;
CREATE POLICY "Allow anon all on daily_reports" ON daily_reports FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE cash_flow ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on cash_flow" ON cash_flow;
CREATE POLICY "Allow anon all on cash_flow" ON cash_flow FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE activity_alerts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on activity_alerts" ON activity_alerts;
CREATE POLICY "Allow anon all on activity_alerts" ON activity_alerts FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE removal_motions ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on removal_motions" ON removal_motions;
CREATE POLICY "Allow anon all on removal_motions" ON removal_motions FOR ALL TO anon USING (true) WITH CHECK (true);

ALTER TABLE system_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow anon all on system_logs" ON system_logs;
CREATE POLICY "Allow anon all on system_logs" ON system_logs FOR ALL TO anon USING (true) WITH CHECK (true);

-- Enable Realtime Replication for Tables
ALTER PUBLICATION supabase_realtime ADD TABLE company_profile;
ALTER PUBLICATION supabase_realtime ADD TABLE partners;
ALTER PUBLICATION supabase_realtime ADD TABLE projects;
ALTER PUBLICATION supabase_realtime ADD TABLE project_components;
ALTER PUBLICATION supabase_realtime ADD TABLE daily_reports;
ALTER PUBLICATION supabase_realtime ADD TABLE cash_flow;
ALTER PUBLICATION supabase_realtime ADD TABLE activity_alerts;
ALTER PUBLICATION supabase_realtime ADD TABLE removal_motions;
ALTER PUBLICATION supabase_realtime ADD TABLE system_logs;

-- Seed default company profile
INSERT INTO company_profile (id, company_name, tagline, logo_url, registration_number, gstin, official_email, official_phone, office_address, website, updated_by_partner)
VALUES (
    1,
    'Dakshyam Innovations',
    'Engineering Next-Gen Robotics & Intelligent Systems',
    '',
    'U72900MH2024PTC123456',
    '27AABCD1234E1Z5',
    'contact@dakshyam.com',
    '+91 98765 43210',
    'Technology Incubation Park, Suite 402, India',
    'https://dakshyam.com',
    'Ankush Nandagouli'
) ON CONFLICT (id) DO NOTHING;
