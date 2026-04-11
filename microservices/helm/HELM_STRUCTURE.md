helm/
│
├── library/                         # 🔥 ZERO duplication layer
│   └── common/
│       ├── Chart.yaml (type: library)
│       └── templates/
│           ├── _deployment.tpl
│           ├── _statefulset.tpl
│           ├── _service.tpl
│           ├── _config.tpl
│           ├── _secret.tpl
│           ├── _probes.tpl
│           └── _helpers.tpl
│
├── infrastructure/
│   ├── kafka/
│   ├── rabbitmq/
│   ├── redis/
│   └── keycloak/
│
├── platform/
│   ├── configserver/
│   ├── eurekaserver/
│   └── gatewayserver/
│
├── databases/
│   ├── accountsdb/
│   ├── cardsdb/
│   └── loansdb/
│
├── microservices/
│   ├── accounts/
│   ├── cards/
│   ├── loans/
│   └── message/
│
├── global/
│   ├── config/
│   │   └── configmap/
│   └── secrets/
│
├── umbrella/                        # 🔥 deploy everything
│   ├── Chart.yaml
│   └── values.yaml
│
└── environments/
├── dev.yaml
├── staging.yaml
└── prod.yaml


| Template           | Used for            |
| ------------------ | ------------------- |
| `_deployment.tpl`  | microservices       |
| `_statefulset.tpl` | DB, Kafka, RabbitMQ |
| `_service.tpl`     | all services        |
| `_config.tpl`      | configmaps          |
| `_secret.tpl`      | secrets             |


1. cd helm/umbrella

2. Clean old state
rm -rf charts/ Chart.lock
(Windows PowerShell 👇)
Remove-Item -Recurse -Force charts, Chart.lock

3. 
    helm dependency build

4. 
    helm install easybites . -f ../environments/dev.yaml