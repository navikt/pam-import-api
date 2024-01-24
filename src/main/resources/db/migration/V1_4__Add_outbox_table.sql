CREATE TABLE ad_outbox
(
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                VARCHAR(36)     NOT NULL,
    payload             TEXT            NOT NULL,
    opprettet_dato      TIMESTAMP       NOT NULL        DEFAULT current_timestamp,
    har_feilet          BOOLEAN         NOT NULL        DEFAULT FALSE,
    antall_forsok       INTEGER         NOT NULL        DEFAULT 0,
    siste_forsok_dato   TIMESTAMP       NULL,
    prosessert_dato     TIMESTAMP       NULL
);

create index IF NOT EXISTS ad_outbox_opprettet_idx on ad_outbox(opprettet_dato);
create index IF NOT EXISTS ad_outbox_prossesert_idx on ad_outbox(prosessert_dato);

