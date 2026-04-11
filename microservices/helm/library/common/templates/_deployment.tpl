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

    spec:
      containers:
        - name: {{ .Values.name }}
          image: {{ .Values.image }}
          imagePullPolicy: {{ .Values.imagePullPolicy | default "IfNotPresent" }}

          {{- if .Values.ports }}
          ports:
            {{- range .Values.ports }}
            - containerPort: {{ . }}
            {{- end }}
          {{- end }}

          # 🔥 ENV HANDLING (GLOBAL + SERVICE-SPECIFIC)
          env:
            # ✅ Global base env (for ALL services)
            - name: SPRING_RABBITMQ_HOST
              value: {{ .Values.global.rabbitmq.host | quote }}

            - name: JAVA_TOOL_OPTIONS
              value: {{ .Values.global.otel.javaAgent | quote }}

            - name: OTEL_EXPORTER_OTLP_ENDPOINT
              value: {{ .Values.global.otel.endpoint | quote }}

            - name: OTEL_METRICS_EXPORTER
              value: "none"

            - name: OTEL_LOGS_EXPORTER
              value: "none"

            {{- if ne .Values.type "configserver" }}
            # ✅ Only for microservices + platform (NOT configserver)
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

            # ✅ Service-specific env
            {{- if .Values.env }}
{{ toYaml .Values.env | nindent 12 }}
            {{- end }}

          {{- if .Values.readinessProbe }}
          readinessProbe:
{{ toYaml .Values.readinessProbe | nindent 12 }}
          {{- end }}

          {{- if .Values.livenessProbe }}
          livenessProbe:
{{ toYaml .Values.livenessProbe | nindent 12 }}
          {{- end }}

          {{- if .Values.resources }}
          resources:
{{ toYaml .Values.resources | nindent 12 }}
          {{- end }}

{{- end }}