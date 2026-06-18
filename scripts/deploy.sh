#!/bin/bash
# Deploy ChatDM to Cloud Run
# Usage: ./scripts/deploy.sh [PROJECT_ID] [REGION]

set -e

PROJECT_ID="${1:-${GOOGLE_CLOUD_PROJECT}}"
REGION="${2:-europe-north1}"
SERVICE_NAME="chatdm"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/chatdm/chatdm:latest"

if [ -z "$PROJECT_ID" ]; then
  echo "Usage: $0 PROJECT_ID [REGION]"
  echo "  Or set GOOGLE_CLOUD_PROJECT environment variable"
  exit 1
fi

echo "=== Deploying ChatDM to Cloud Run ==="
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo ""

# Configure Docker for Artifact Registry
echo "Configuring Docker for Artifact Registry..."
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# Build and push image
echo "Building Docker image..."
docker build -t "$IMAGE" .

echo "Pushing to Artifact Registry..."
docker push "$IMAGE"

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
gcloud run deploy "$SERVICE_NAME" \
  --image="$IMAGE" \
  --region="$REGION" \
  --platform=managed \
  --allow-unauthenticated \
  --memory=2Gi \
  --timeout=3600 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=cloud,GOOGLE_CLOUD_PROJECT=$PROJECT_ID" \
  --project="$PROJECT_ID"

echo ""
echo "=== Deployment complete ==="
gcloud run services describe "$SERVICE_NAME" --region="$REGION" --project="$PROJECT_ID" --format='value(status.url)'
