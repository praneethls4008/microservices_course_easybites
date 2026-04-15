{{- define "common.daemonset" }}
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: {{ .Values.name }}
  labels:
    app: {{ .Values.name }}

spec:
  selector:
    matchLabels:
      app: {{ .Values.name }}

  template:
    metadata:
      labels:
        app: {{ .Values.name }}

    spec:
      {{- if .Values.serviceAccountName }}
      serviceAccountName: {{ .Values.serviceAccountName }}
      {{- end }}

      containers:
        - name: {{ .Values.name }}
          image: {{ .Values.image }}
          imagePullPolicy: IfNotPresent

          args:
            {{- toYaml .Values.args | nindent 12 }}

          ports:
            - containerPort: 12345

          volumeMounts:
            - name: config
              mountPath: /etc/alloy/config.alloy
              subPath: config.alloy

      volumes:
        - name: config
          configMap:
            name: alloy-config
{{- end }}