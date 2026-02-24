MCP for solo TTRPGs

## Local development with GCS

PDFs, search index, and journal data live in Google Cloud Storage. To run locally against cloud data:

```bash
SPRING_PROFILES_ACTIVE=gcs CHATDM_GCS_BUCKET=your-bucket ./mvnw spring-boot:run
```

Or with `GOOGLE_CLOUD_PROJECT` set (uses `{project}-chatdm-resources` bucket). Requires `gcloud auth application-default login`.