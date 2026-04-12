{{- define "common.deployment" }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Values.name }}
  labels:
    app: {{ .Values.name }}

spec:
  replicas: {{ .Values.replicaCount | default 1 }}

  selector:
    matchLabels:
      app: {{ .Values.name }}

  template:
    metadata:
      labels:
        app: {{ .Values.name }}
      {{- if .Values.podAnnotations }}
      annotations:
        {{- toYaml .Values.podAnnotations | nindent 8 }}
      {{- end }}

    spec:

      {{- if .Values.serviceAccountName }}
      serviceAccountName: {{ .Values.serviceAccountName }}
      {{- end }}

      {{- if .Values.initContainers }}
      initContainers:
        {{- toYaml .Values.initContainers | nindent 8 }}
      {{- end }}

      containers:
        - name: {{ .Values.name }}
          image: {{ .Values.image }}
          imagePullPolicy: {{ .Values.imagePullPolicy | default "IfNotPresent" }}

          {{- if .Values.command }}
          command:
            {{- toYaml .Values.command | nindent 12 }}
          {{- end }}

          {{- if .Values.args }}
          args:
            {{- toYaml .Values.args | nindent 12 }}
          {{- end }}

          {{- if .Values.ports }}
          ports:
            {{- range .Values.ports }}
            - containerPort: {{ . }}
            {{- end }}
          {{- end }}

          {{- if not .Values.skipGlobalEnv }}
          # Global base env — injected for all Spring Boot services.
          # Set skipGlobalEnv: true in values.yaml to skip (e.g. observability tools).
          env:
            - name: SPRING_RABBITMQ_HOST
              value: {{ .Values.global.rabbitmq.host | quote }}

            - name: JAVA_TOOL_OPTIONS
              {{- if or (eq .Values.name "keycloak") (eq .Values.name "keycloak-ui") }}
              value: ""
              {{- else if .Values.global.otel.skipJavaAgent }}
              value: ""
              {{- else }}
              value: {{ .Values.global.otel.javaAgent | quote }}
              {{- end }}

            - name: OTEL_EXPORTER_OTLP_ENDPOINT
              value: {{ .Values.global.otel.endpoint | quote }}

            - name: OTEL_METRICS_EXPORTER
              value: "none"

            - name: OTEL_LOGS_EXPORTER
              value: "none"

            {{- if ne .Values.type "configserver" }}
            # Only for microservices + platform (NOT configserver itself)
            - name: SPRING_PROFILES_ACTIVE
              value: {{ .Values.global.spring.profile | quote }}

            - name: SPRING_CONFIG_IMPORT
              value: {{ printf "configserver:http://configserver:%v/" .Values.global.ports.configserver | quote }}

            - name: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE
              value: {{ .Values.global.eureka.url | quote }}

            - name: EUREKA_INSTANCE_PREFER_IP_ADDRESS
              value: "true"

            - name: EUREKA_INSTANCE_HOSTNAME
              value: {{ .Values.global.eureka.hostname | quote }}
            {{- end }}

            # Service-specific env vars
            {{- if .Values.env }}
            {{- toYaml .Values.env | nindent 12 }}
            {{- end }}

          {{- else }}
          {{- /* skipGlobalEnv=true: only service-specific env vars, no Spring/OTEL injection */}}
          {{- if .Values.env }}
          env:
            {{- toYaml .Values.env | nindent 12 }}
          {{- end }}
          {{- end }}

          {{- if .Values.readinessProbe }}
          readinessProbe:
            {{- toYaml .Values.readinessProbe | nindent 12 }}
          {{- end }}

          {{- if .Values.livenessProbe }}
          livenessProbe:
            {{- toYaml .Values.livenessProbe | nindent 12 }}
          {{- end }}

          {{- if .Values.resources }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          {{- end }}

          {{- if .Values.volumeMounts }}
          volumeMounts:
            {{- toYaml .Values.volumeMounts | nindent 12 }}
          {{- end }}

      {{- if .Values.volumes }}
      volumes:
        {{- toYaml .Values.volumes | nindent 8 }}
      {{- end }}

{{- end }}
