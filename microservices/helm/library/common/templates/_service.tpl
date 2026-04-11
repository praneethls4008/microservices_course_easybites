{{- define "common.service" }}
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.name }}
spec:
  type: {{ .Values.service.type | default "ClusterIP" }}
  selector:
    app: {{ .Values.name }}
  ports:
    {{- range .Values.service.ports }}
    - port: {{ .port }}
      targetPort: {{ .targetPort }}
      {{- if .name }}
      name: {{ .name }}
      {{- end }}
    {{- end }}
{{- end }}