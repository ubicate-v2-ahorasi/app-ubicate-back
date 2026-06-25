-- Registro de "pasos" del bus por cada parada de su ruta.
-- NOTA: los perfiles docker y prod usan ddl-auto: update, asi que Hibernate
-- crea esta tabla automaticamente al desplegar. Script solo de referencia.

CREATE TABLE IF NOT EXISTS route_stop_passages (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    ruta_id                  BIGINT       NOT NULL,
    route_stop_id            BIGINT       NOT NULL,
    bus_id                   BIGINT       NOT NULL,
    placa                    VARCHAR(20),
    orden                    INT,
    nombre_parada            VARCHAR(120),
    hora_cruce               DATETIME(6)  NOT NULL,
    segundos_desde_anterior  INT,
    PRIMARY KEY (id),
    KEY idx_passage_ruta_bus (ruta_id, bus_id, hora_cruce),
    KEY idx_passage_ruta (ruta_id, hora_cruce)
);
