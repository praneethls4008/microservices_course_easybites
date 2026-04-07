k8s/
│
├── base/                          # Base reusable manifests (no env-specific values)
│   ├── config/                   # Global configs
│   │   ├── configmap.yaml
│   │   └── secret.yaml
│   │
│   ├── infrastructure/           # Shared infra services
│   │   ├── kafka/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── pvc.yaml
│   │   │
│   │   ├── rabbitmq/
│   │   ├── redis/
│   │   ├── postgres/
│   │   └── keycloak/
│   │
│   ├── databases/                # Each DB separated
│   │   ├── accountsdb/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── pvc.yaml
│   │   │
│   │   ├── cardsdb/
│   │   └── loansdb/
│   │
│   ├── platform/                 # Core platform services
│   │   ├── configserver/
│   │   ├── eurekaserver/
│   │   └── gatewayserver/
│   │
│   ├── microservices/            # Business services
│   │   ├── accounts/
│   │   ├── cards/
│   │   ├── loans/
│   │   └── message/
│   │
│   └── common/                   # Reusable pieces (optional advanced)
│       ├── labels.yaml
│       ├── resource-limits.yaml
│       └── probes.yaml
│
├── overlays/                     # Environment-specific configs
│   ├── dev/
│   │   ├── kustomization.yaml
│   │   ├── config-patch.yaml
│   │   └── replicas-patch.yaml
│   │
│   ├── staging/
│   │   ├── kustomization.yaml
│   │   └── patches/
│   │
│   └── prod/
│       ├── kustomization.yaml
│       ├── hpa.yaml              # Auto-scaling
│       ├── ingress.yaml          # External access
│       └── resource-limits.yaml
│
├── helm/                         # (Optional) Helm charts if you use Helm
│   ├── kafka/
│   ├── keycloak/
│   └── redis/
│
├── scripts/                      # Helper scripts
│   ├── deploy.sh
│   ├── delete.sh
│   └── port-forward.sh
│
└── README.md                     # Documentation