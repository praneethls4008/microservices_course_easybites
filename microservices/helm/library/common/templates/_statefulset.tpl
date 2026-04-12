{{- define "common.statefulset" }}
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: {{ .Values.name }}
  labels:
    app: {{ .Values.name }}
    {{- if .Values.labels }}
    {{- toYaml .Values.labels | nindent 4 }}
    {{- end }}

spec:
  serviceName: {{ .Values.name }}
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

      {{- if .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml .Values.imagePullSecrets | nindent 8 }}
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
            {{- if kindIs "map" . }}
            - name: {{ .name }}
              containerPort: {{ .containerPort }}
            {{- else }}
            - containerPort: {{ . }}
            {{- end }}
            {{- end }}
          {{- end }}


          {{- if .Values.env }}
          env:
            {{- toYaml .Values.env | nindent 12 }}
          {{- end }}

          {{- if .Values.envFrom }}
          envFrom:
            {{- toYaml .Values.envFrom | nindent 12 }}
          {{- end }}

          {{- if .Values.volumeMounts }}
          volumeMounts:
            {{- toYaml .Values.volumeMounts | nindent 12 }}
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

          {{- if .Values.securityContext }}
          securityContext:
            {{- toYaml .Values.securityContext | nindent 12 }}
          {{- end }}

      {{- if .Values.nodeSelector }}
      nodeSelector:
        {{- toYaml .Values.nodeSelector | nindent 8 }}
      {{- end }}

      {{- if .Values.affinity }}
      affinity:
        {{- toYaml .Values.affinity | nindent 8 }}
      {{- end }}

      {{- if .Values.tolerations }}
      tolerations:
        {{- toYaml .Values.tolerations | nindent 8 }}
      {{- end }}

      {{- if .Values.volumes }}
      volumes:
        {{- toYaml .Values.volumes | nindent 8 }}
      {{- end }}

  {{- /* SAFE persistence handling */}}
  {{- if (and .Values.persistence (ne (default true .Values.persistence.enabled) false)) }}
  volumeClaimTemplates:
    - metadata:
        name: {{ .Values.persistence.name | default "data" }}
      spec:
        accessModes:
          - {{ .Values.persistence.accessMode | default "ReadWriteOnce" }}
        {{- if .Values.persistence.storageClass }}
        storageClassName: {{ .Values.persistence.storageClass }}
        {{- end }}
        resources:
          requests:
            storage: {{ default "1Gi" (coalesce .Values.persistence.size .Values.storage) }}
  {{- end }}

{{- end }}