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
      serviceAccountName: {{ .Values.serviceAccountName }}

      containers:
        - name: {{ .Values.name }}
          image: {{ .Values.image }}
          imagePullPolicy: IfNotPresent

          args:
            {{- toYaml .Values.args | nindent 12 }}

          securityContext:
            runAsUser: 0

          volumeMounts:
            - name: config
              mountPath: /etc/alloy/config.alloy
              subPath: config.alloy

            - name: containers
              mountPath: /var/log/containers
              readOnly: true

            - name: pods
              mountPath: /var/log/pods
              readOnly: true

      volumes:
        - name: config
          configMap:
            name: alloy-config

        - name: containers
          hostPath:
            path: /var/log/containers

        - name: pods
          hostPath:
            path: /var/log/pods
{{- end }}