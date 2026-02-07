#!/bin/bash

# k6 Load Test Runner
# Usage: ./run.sh [scenario] [environment]
# Examples:
#   ./run.sh smoke local
#   ./run.sh load docker
#   ./run.sh stress staging

set -e

SCENARIO=${1:-smoke}
ENV=${2:-local}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_DIR="results"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validate scenario
if [ ! -f "scenarios/${SCENARIO}.js" ]; then
    echo -e "${RED}Error: Scenario '${SCENARIO}' not found${NC}"
    echo "Available scenarios:"
    ls -1 scenarios/*.js | sed 's/scenarios\//  - /' | sed 's/\.js//'
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

echo -e "${YELLOW}================================${NC}"
echo -e "${YELLOW}k6 Load Test Runner${NC}"
echo -e "${YELLOW}================================${NC}"
echo ""
echo -e "Scenario:    ${GREEN}${SCENARIO}${NC}"
echo -e "Environment: ${GREEN}${ENV}${NC}"
echo -e "Timestamp:   ${TIMESTAMP}"
echo ""

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: k6 is not installed${NC}"
    echo "Install with: brew install k6"
    exit 1
fi

# Run the test
echo -e "${YELLOW}Starting test...${NC}"
echo ""

k6 run \
  --env TARGET_ENV="$ENV" \
  --out json="$OUTPUT_DIR/${SCENARIO}_${ENV}_${TIMESTAMP}.json" \
  --summary-export="$OUTPUT_DIR/${SCENARIO}_${ENV}_${TIMESTAMP}_summary.json" \
  "scenarios/${SCENARIO}.js"

EXIT_CODE=$?

echo ""
echo -e "${YELLOW}================================${NC}"
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}Test completed successfully!${NC}"
else
    echo -e "${RED}Test failed with exit code: ${EXIT_CODE}${NC}"
fi
echo -e "${YELLOW}================================${NC}"
echo ""
echo "Results saved to:"
echo "  - $OUTPUT_DIR/${SCENARIO}_${ENV}_${TIMESTAMP}.json"
echo "  - $OUTPUT_DIR/${SCENARIO}_${ENV}_${TIMESTAMP}_summary.json"

exit $EXIT_CODE
