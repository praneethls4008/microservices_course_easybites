# Helm Structure

```
helm/
│
├── library/                         # Zero-duplication shared templates
│   └── common/
│       ├── Chart.yaml (type: library)
│       └── templates/
│           ├── _deployment.tpl      # Deployments (microservices + platform)
│           ├── _statefulset.tpl     # StatefulSets (DBs, Kafka, RabbitMQ, Redis)
│           ├── _service.tpl         # Services — supports ClusterIP + NodePort
│           ├── _config.tpl          # ConfigMaps
│           ├── _secret.tpl          # Secrets
│           └── _helpers.tpl
│
├── global/
│   └── secrets-chart/               # Helm chart → deploys global-secret + keycloak-secret
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/secrets.yaml
│
├── infrastructure/
│   ├── rabbitmq/     (StatefulSet)
│   ├── kafka/        (StatefulSet — all KAFKA_* env vars included)
│   ├── redis/        (StatefulSet)
│   ├── postgres/     (StatefulSet — backing DB for Keycloak)
│   └── keycloak/     (Deployment — waits for postgres)
│
├── platform/
│   ├── configserver/   (Deployment — waits for rabbitmq)
│   ├── eurekaserver/   (Deployment — waits for configserver)
│   └── gatewayserver/  (Deployment — waits for configserver, eureka, redis, keycloak)
│
├── databases/
│   ├── accountsdb/   (StatefulSet — MySQL)
│   ├── cardsdb/      (StatefulSet — MySQL)
│   └── loansdb/      (StatefulSet — MySQL)
│
├── microservices/
│   ├── accounts/     (Deployment — waits for configserver + eureka + accountsdb)
│   ├── cards/        (Deployment — waits for configserver + eureka + cardsdb)
│   ├── loans/        (Deployment — waits for configserver + eureka + loansdb)
│   └── message/      (Deployment — waits for configserver + eureka)
│
├── umbrella/                        # Deploy everything as one release
│   ├── Chart.yaml
│   └── values.yaml
│
└── environments/
    ├── dev.yaml      # localhost — spring profile=dev, NodePort services
    ├── staging.yaml
    └── prod.yaml
```

## Template Usage

| Template           | Used for                          |
| ------------------ | --------------------------------- |
| `_deployment.tpl`  | microservices + platform services |
| `_statefulset.tpl` | DB, Kafka, RabbitMQ, Redis        |
| `_service.tpl`     | all (ClusterIP + NodePort)        |

## Deploy on localhost (dev profile)

### Prerequisites
- `kubectl` pointing at your local cluster (Docker Desktop / minikube / kind)
- Helm 3.x installed

### Steps

```powershell
# 1. Go to umbrella
cd helm/umbrella

# 2. Clean any previous build artifacts
Remove-Item -Recurse -Force charts, Chart.lock -ErrorAction SilentlyContinue

# 3. Build all sub-chart dependencies
helm dependency build

# 4. Install with dev profile (NodePort + spring.profile=dev)
helm install myapp . -f ../environments/dev.yaml

# 5. To upgrade after any changes
helm upgrade myapp . -f ../environments/dev.yaml
```

### Local access after deploy

| Service      | URL                      |
| ------------ | ------------------------ |
| Gateway      | http://localhost:30001   |
| Eureka UI    | http://localhost:30061   |
| Keycloak     | http://localhost:30080   |
| ConfigServer | http://localhost:30801   |

### Uninstall

```powershell
helm uninstall myapp
```

## What flows where

Global config (rabbitmq host, otel endpoint, eureka URL, spring profile) flows
down automatically from `umbrella/values.yaml` → `global.*` → every sub-chart.

`environments/dev.yaml` overrides:
- `global.spring.profile: dev`  ← activates Spring dev profile
- NodePort numbers for externally reachable services


Delete All in kubenetes
kubectl delete all --all

upgrade release:
helm upgrade easybites-v1 . -f ../environments/dev.yaml

install release
helm install easybites-v1 . -f ../environments/dev.yaml

uninstall release
helm uninstall easybites-v1
Remove-Item -Recurse -Force charts, Chart.lock

.\scripts\build-all.ps1
