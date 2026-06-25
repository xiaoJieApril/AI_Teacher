-- Prototype schema for the Android AI Teacher app.
-- This first version is anonymous single-device sync. For production, enable
-- Supabase Auth and replace the prototype policies with user-scoped RLS.

create table if not exists public.chat_messages (
  remote_id uuid primary key,
  device_id text not null,
  role text not null,
  message text not null,
  action_type text not null default 'general_chat',
  subject text not null default 'none',
  score text not null default 'N/A',
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.learning_tasks (
  remote_id uuid primary key,
  device_id text not null,
  title text not null,
  description text not null,
  subject text not null,
  suggested_minutes integer not null,
  completion_standard text not null default '',
  next_action_type text not null default 'none',
  next_action_instruction text not null default '',
  status text not null default 'todo',
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.schedule_items (
  remote_id uuid primary key,
  device_id text not null,
  date text not null,
  start_time text not null,
  end_time text not null,
  subject text not null,
  title text not null,
  description text not null,
  suggested_minutes integer not null,
  completion_standard text not null,
  requires_focus_timer boolean not null default true,
  status text not null default 'todo',
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.homework_submissions (
  remote_id uuid primary key,
  device_id text not null,
  subject text not null,
  prompt text not null,
  feedback text not null,
  score text not null,
  strengths text not null default '',
  problems text not null default '',
  corrections text not null default '',
  next_action_instruction text not null default '',
  image_uri text,
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.focus_sessions (
  remote_id uuid primary key,
  device_id text not null,
  planned_minutes integer not null,
  completed_seconds integer not null,
  completed boolean not null,
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.homework_drafts (
  remote_id uuid primary key,
  device_id text not null,
  source_type text not null,
  source_remote_id text not null,
  subject text not null,
  title text not null,
  prompt text not null,
  completion_standard text not null default '',
  draft_text text not null default '',
  status text not null default 'draft',
  created_at bigint not null,
  updated_at_ms bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.user_profiles (
  remote_id uuid primary key,
  device_id text not null,
  nickname text not null default '',
  learning_goal text not null default '',
  timezone text not null default 'Asia/Kuala_Lumpur',
  updated_at_ms bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.availability_rules (
  remote_id uuid primary key,
  device_id text not null,
  weekday text not null,
  start_time text not null,
  end_time text not null,
  label text not null,
  rule_type text not null default 'work',
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.availability_exceptions (
  remote_id uuid primary key,
  device_id text not null,
  date text not null,
  start_time text not null,
  end_time text not null,
  label text not null,
  rule_type text not null default 'unavailable',
  created_at bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.social_publishing_assignments (
  remote_id uuid primary key,
  device_id text not null,
  title text not null,
  description text not null,
  month text not null,
  due_date text not null,
  required_platforms text not null default 'X,Pixiv',
  artwork_notes text not null default '',
  status text not null default 'active',
  created_at bigint not null,
  updated_at_ms bigint not null,
  updated_at timestamptz not null default now()
);

create table if not exists public.social_post_proofs (
  remote_id uuid primary key,
  device_id text not null,
  assignment_remote_id text not null,
  platform text not null,
  url text not null,
  verification_status text not null default 'pending',
  ai_feedback text not null default '',
  submitted_at bigint not null,
  updated_at timestamptz not null default now()
);

create index if not exists chat_messages_device_idx on public.chat_messages(device_id);
create index if not exists learning_tasks_device_idx on public.learning_tasks(device_id);
create index if not exists schedule_items_device_date_idx on public.schedule_items(device_id, date);
create index if not exists homework_submissions_device_idx on public.homework_submissions(device_id);
create index if not exists focus_sessions_device_idx on public.focus_sessions(device_id);
create index if not exists homework_drafts_device_idx on public.homework_drafts(device_id);
create index if not exists user_profiles_device_idx on public.user_profiles(device_id);
create index if not exists availability_rules_device_idx on public.availability_rules(device_id);
create index if not exists availability_exceptions_device_date_idx on public.availability_exceptions(device_id, date);
create index if not exists social_assignments_device_idx on public.social_publishing_assignments(device_id);
create index if not exists social_proofs_device_assignment_idx on public.social_post_proofs(device_id, assignment_remote_id);

-- Single-owner RLS setup.
-- Create one Supabase Auth user in Authentication > Users:
-- email: xiaoJieApril@learning-app.local
-- password: keep it only in local.properties as SUPABASE_AUTH_PASSWORD
--
-- The Android app signs in with SUPABASE_AUTH_EMAIL/SUPABASE_AUTH_PASSWORD,
-- then all synced rows are owned by auth.uid(). The old device_id remains for
-- local prototype grouping, but RLS security is based on the authenticated user.

alter table public.chat_messages add column if not exists user_id uuid default auth.uid();
alter table public.learning_tasks add column if not exists user_id uuid default auth.uid();
alter table public.schedule_items add column if not exists user_id uuid default auth.uid();
alter table public.homework_submissions add column if not exists user_id uuid default auth.uid();
alter table public.focus_sessions add column if not exists user_id uuid default auth.uid();
alter table public.homework_drafts add column if not exists user_id uuid default auth.uid();
alter table public.user_profiles add column if not exists user_id uuid default auth.uid();
alter table public.availability_rules add column if not exists user_id uuid default auth.uid();
alter table public.availability_exceptions add column if not exists user_id uuid default auth.uid();
alter table public.social_publishing_assignments add column if not exists user_id uuid default auth.uid();
alter table public.social_post_proofs add column if not exists user_id uuid default auth.uid();

alter table public.chat_messages enable row level security;
alter table public.learning_tasks enable row level security;
alter table public.schedule_items enable row level security;
alter table public.homework_submissions enable row level security;
alter table public.focus_sessions enable row level security;
alter table public.homework_drafts enable row level security;
alter table public.user_profiles enable row level security;
alter table public.availability_rules enable row level security;
alter table public.availability_exceptions enable row level security;
alter table public.social_publishing_assignments enable row level security;
alter table public.social_post_proofs enable row level security;

drop policy if exists "prototype anon access" on public.chat_messages;
drop policy if exists "single owner access" on public.chat_messages;
create policy "single owner access" on public.chat_messages for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.learning_tasks;
drop policy if exists "single owner access" on public.learning_tasks;
create policy "single owner access" on public.learning_tasks for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.schedule_items;
drop policy if exists "single owner access" on public.schedule_items;
create policy "single owner access" on public.schedule_items for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.homework_submissions;
drop policy if exists "single owner access" on public.homework_submissions;
create policy "single owner access" on public.homework_submissions for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.focus_sessions;
drop policy if exists "single owner access" on public.focus_sessions;
create policy "single owner access" on public.focus_sessions for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.homework_drafts;
drop policy if exists "single owner access" on public.homework_drafts;
create policy "single owner access" on public.homework_drafts for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.user_profiles;
drop policy if exists "single owner access" on public.user_profiles;
create policy "single owner access" on public.user_profiles for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.availability_rules;
drop policy if exists "single owner access" on public.availability_rules;
create policy "single owner access" on public.availability_rules for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.availability_exceptions;
drop policy if exists "single owner access" on public.availability_exceptions;
create policy "single owner access" on public.availability_exceptions for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.social_publishing_assignments;
drop policy if exists "single owner access" on public.social_publishing_assignments;
create policy "single owner access" on public.social_publishing_assignments for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists "prototype anon access" on public.social_post_proofs;
drop policy if exists "single owner access" on public.social_post_proofs;
create policy "single owner access" on public.social_post_proofs for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
