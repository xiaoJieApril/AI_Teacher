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

create index if not exists chat_messages_device_idx on public.chat_messages(device_id);
create index if not exists learning_tasks_device_idx on public.learning_tasks(device_id);
create index if not exists schedule_items_device_date_idx on public.schedule_items(device_id, date);
create index if not exists homework_submissions_device_idx on public.homework_submissions(device_id);
create index if not exists focus_sessions_device_idx on public.focus_sessions(device_id);

-- Prototype option: keep RLS disabled while testing with an anon key.
-- Production option: enable RLS and bind rows to auth.uid() instead of device_id.
