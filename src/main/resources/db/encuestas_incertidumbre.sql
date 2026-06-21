-- Tabla para el cuestionario de incertidumbre enviado desde la app movil.
-- NOTA: los perfiles docker y prod usan ddl-auto: update, asi que Hibernate
-- crea esta tabla automaticamente al desplegar. Este script es solo referencia
-- (o por si en algun momento se adopta Flyway / creacion manual).

CREATE TABLE IF NOT EXISTS encuestas_incertidumbre (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    pregunta1      INT          NOT NULL,
    pregunta2      INT          NOT NULL,
    pregunta3      INT          NOT NULL,
    pregunta4      INT          NOT NULL,
    promedio       DOUBLE       NOT NULL,
    nivel          VARCHAR(10)  NOT NULL,
    fecha_creacion DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);
