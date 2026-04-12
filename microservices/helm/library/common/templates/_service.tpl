{{- define "common.service" }}
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.name }}
spec:
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

      {{- if and (eq $.Values.service.type "NodePort") .nodePort }}
      nodePort: {{ .nodePort }}
      {{- end }}

    {{- end }}
{{- end }}