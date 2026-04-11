{{- define "common.service" }}
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.name }}
spec:
  # Check if headless is requested, otherwise use the type (defaulting to ClusterIP)
  {{- if .Values.service.headless }}
  clusterIP: None
  {{- else }}
  type: {{ .Values.service.type | default "ClusterIP" }}
  {{- end }}
  selector:
    app: {{ .Values.name }}
  ports:
    {{- range .Values.service.ports }}
    - port: {{ .port }}
      targetPort: {{ .targetPort }}
      protocol: {{ .protocol | default "TCP" }}
      {{- if .name }}
      name: {{ .name }}
      {{- end }}
    {{- end }}
{{- end }}