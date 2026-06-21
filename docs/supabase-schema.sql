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

-- Prototype option: keep RLS disabled while testing with an anon key.
-- Production option: enable RLS and bind rows to auth.uid() instead of device_id.
