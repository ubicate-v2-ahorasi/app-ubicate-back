# 🚀 Despliegue en Dockploy - GeoRoute API

## Archivos necesarios

```
App-JWT-Auth-Backend/
├── Dockerfile                    # Build de la imagen
├── docker-compose.yml            # Desarrollo local completo
├── docker-compose.dockploy.yml   # Para Dockploy (sin servicios managed)
├── .env                          # Variables de entorno
└── src/main/resources/
    └── application-docker.yml    # Configuración Docker
```

## Pasos para desplegar en Dockploy

### 1. Configurar variables de entorno en Dockploy

Desde el panel de Dockploy, configura estas variables:

| Variable | Valor ejemplo | Descripción |
|----------|---------------|-------------|
| `PORT` | `8080` | Puerto del contenedor |
| `SPRING_PROFILES_ACTIVE` | `docker` | Perfil de Spring |
| `DB_HOST` | `DB_HOST_DE_DOCKPLOY` | Host de MySQL (provided by Dockploy) |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_USERNAME` | `georoute_user` | Usuario de MySQL |
| `DB_PASSWORD` | `TuPasswordSeguro123!` | Contraseña MySQL |
| `DB_NAME` | `georoute_db` | Nombre de la base de datos |
| `REDIS_HOST` | `REDIS_HOST_DE_DOCKPLOY` | Host de Redis |
| `REDIS_PORT` | `6379` | Puerto de Redis |
| `RABBITMQ_HOST` | `RABBITMQ_HOST_DE_DOCKPLOY` | Host de RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Puerto de RabbitMQ |
| `RABBITMQ_USERNAME` | `georoute` | Usuario de RabbitMQ |
| `RABBITMQ_PASSWORD` | `TuPasswordMQ123!` | Contraseña RabbitMQ |
| `JWT_SECRET` | `(generar clave larga)` | Clave secreta JWT |
| `FIREBASE_DATABASE_URL` | `https://tu-proyecto.firebaseio.com` | URL de Firebase |
| `CORS_ORIGINS` | `http://localhost:8100,https://tu-dominio.com` | Dominios permitidos |

### 2. Subir archivos a Dockploy

Sube estos archivos/carpetas:
- `Dockerfile`
- `docker-compose.dockploy.yml` (renómbralo a `docker-compose.yml` en Dockploy)
- `.env` (o configura las variables directamente en Dockploy)
- `src/main/resources/` (para el firebase-service-account.json si lo usas)

### 3. Build y deploy

Desde Dockploy:
1. Selecciona el `Dockerfile`
2. Configura el puerto: `8080`
3. Agrega las variables de entorno
4. Deploy!

### 4. Verificar despliegue

```bash
# Health check
curl https://tu-dominio.com/actuator/health

# Probar endpoint
curl https://tu-dominio.com/api/public/empresas
```

## Notas importantes

- El `docker-compose.dockploy.yml` **NO** incluye MySQL, Redis ni RabbitMQ - estos se configuran como servicios managed en Dockploy
- Asegúrate de que las variables `DB_HOST`, `REDIS_HOST`, `RABBITMQ_HOST` apunten a los servicios managed de Dockploy
- El archivo `firebase-service-account.json` debe estar en `src/main/resources/` para que se copie al contenedor

## Comandos útiles

```bash
# Ver logs
docker logs georoute-api -f

# Reiniciar
docker restart georoute-api

# Ver estado
docker ps | grep georoute
```
