CREATE TABLE IF NOT EXISTS attendants (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  team VARCHAR(30) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS service_requests (
  id BIGSERIAL PRIMARY KEY,
  customer_name VARCHAR(120) NOT NULL,
  subject VARCHAR(200) NOT NULL,
  team VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  assigned_attendant_id BIGINT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_service_requests_attendant
    FOREIGN KEY (assigned_attendant_id) REFERENCES attendants(id)
);

CREATE TABLE IF NOT EXISTS queue_items (
  id BIGSERIAL PRIMARY KEY,
  service_request_id BIGINT NOT NULL UNIQUE,
  team VARCHAR(30) NOT NULL,
  enqueued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_queue_items_request
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id)
);

CREATE INDEX idx_attendants_team_active ON attendants(team, active);
CREATE INDEX idx_service_requests_team_status ON service_requests(team, status);
CREATE INDEX idx_queue_items_team_enqueued ON queue_items(team, enqueued_at, id);
