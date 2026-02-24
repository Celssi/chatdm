#!/bin/bash
# ChatDM GCP Setup Script
# Creates project resources: GCS bucket, Artifact Registry, Cloud Build trigger
# Usage: ./scripts/gcp-setup.sh [PROJECT_ID] [REGION]

set -e

PROJECT_ID="${1:-${GOOGLE_CLOUD_PROJECT}}"
REGION="${2:-europe-north1}"
BUCKET_NAME="${PROJECT_ID}-chatdm-resources"
REPOSITORY="chatdm"

if [ -z "$PROJECT_ID" ]; then
  echo "Usage: $0 PROJECT_ID [REGION]"
  echo "  Or set GOOGLE_CLOUD_PROJECT environment variable"
  exit 1
fi

echo "=== ChatDM GCP Setup ==="
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo "Bucket: $BUCKET_NAME"
echo ""

# Set project
gcloud config set project "$PROJECT_ID"

# Enable APIs
echo "Enabling APIs..."
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  storage.googleapis.com \
  storage-api.googleapis.com \
  cloudbuild.googleapis.com \
  --project="$PROJECT_ID"

# Create GCS bucket for PDFs, index, and journal
echo "Creating GCS bucket..."
if gsutil ls "gs://${BUCKET_NAME}" 2>/dev/null; then
  echo "Bucket gs://${BUCKET_NAME} already exists"
else
  gsutil mb -p "$PROJECT_ID" -l "$REGION" "gs://${BUCKET_NAME}"
  echo "Bucket created: gs://${BUCKET_NAME}"
fi

# Create Artifact Registry repository for Docker images
echo "Creating Artifact Registry repository..."
if gcloud artifacts repositories describe "$REPOSITORY" --location="$REGION" --project="$PROJECT_ID" 2>/dev/null; then
  echo "Repository $REPOSITORY already exists"
else
  gcloud artifacts repositories create "$REPOSITORY" \
    --repository-format=docker \
    --location="$REGION" \
    --project="$PROJECT_ID"
  echo "Repository created: $REGION-docker.pkg.dev/$PROJECT_ID/$REPOSITORY"
fi

echo ""
echo "=== Setup complete ==="
echo ""
echo "Next steps:"
echo "1. Upload PDFs: gsutil -m cp -r src/main/resources/pdfs/* gs://${BUCKET_NAME}/pdfs/"
echo "2. Build and upload search index (after PDFs):"
echo "   ./mvnw exec:java@build-search-index-gcs -Dgcs.bucket=${BUCKET_NAME}"
echo "3. Deploy: ./scripts/deploy.sh $PROJECT_ID $REGION"
echo ""
echo "Cloud Build trigger (deploy on push to main):"
echo "  1. Connect repo: https://console.cloud.google.com/cloud-build/triggers/add?project=${PROJECT_ID}"
echo "  2. Source: GitHub/GitLab, select repo, branch ^main$"
echo "  3. Config: Cloud Build configuration file, cloudbuild.yaml"
