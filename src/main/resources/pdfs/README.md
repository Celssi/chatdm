# PDFs (stored in GCS)

PDFs are stored in Google Cloud Storage, not in this directory.

**Upload PDFs to GCS:**
```bash
gsutil -m cp -r /path/to/your/pdfs/* gs://YOUR_BUCKET/pdfs/
```

**Local development with GCS:**
```bash
SPRING_PROFILES_ACTIVE=gcs CHATDM_GCS_BUCKET=your-bucket ./mvnw spring-boot:run
```

Or set `GOOGLE_CLOUD_PROJECT` for bucket `{project}-chatdm-resources`.
