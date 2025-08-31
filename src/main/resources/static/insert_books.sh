#!/bin/bash

# Book Insertion Script with HTTP Response Validation
# This script inserts books from JSON files and validates the HTTP responses

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_URL="http://localhost:9000/api/books"
EXPECTED_STATUS="201"
MAX_RETRIES=3
RETRY_DELAY=2

echo -e "${BLUE}📚 Book Insertion Script${NC}"
echo "================================"

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

# Function to check if API is available
check_api_availability() {
    local max_attempts=10
    local attempt=1
    
    print_info "Checking if API is available at $API_URL..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s --connect-timeout 5 --max-time 10 "$API_URL" > /dev/null 2>&1; then
            print_status "API is available"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "API is not available after $max_attempts attempts"
    return 1
}

# Function to extract HTTP status code from curl output
extract_http_status() {
    local curl_output="$1"
    echo "$curl_output" | grep -o "HTTP/[0-9\.]\+ [0-9]\+" | tail -1 | awk '{print $2}'
}

# Function to insert a single book
insert_book() {
    local json_file="$1"
    local book_name=$(basename "$json_file" .json)
    local attempt=1
    
    print_info "Inserting book: $book_name"
    
    # Check if file exists
    if [ ! -f "$json_file" ]; then
        print_error "File not found: $json_file"
        return 1
    fi
    
    # Validate JSON format
    if ! jq empty "$json_file" 2>/dev/null; then
        print_error "Invalid JSON format in $json_file"
        return 1
    fi
    
    while [ $attempt -le $MAX_RETRIES ]; do
        # Capture both HTTP response and status code
        local temp_file=$(mktemp)
        local response=$(curl -s -w "\nHTTP_STATUS:%{http_code}\nTIME_TOTAL:%{time_total}" \
            -d "@$json_file" \
            -H "Content-Type: application/json" \
            "$API_URL" \
            -o "$temp_file" 2>&1)
        
        # Extract status code
        local status_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
        local time_total=$(echo "$response" | grep "TIME_TOTAL:" | cut -d: -f2)
        
        # Read response body
        local response_body=$(cat "$temp_file")
        rm -f "$temp_file"
        
        # Check if we got the expected status code
        if [ "$status_code" = "$EXPECTED_STATUS" ]; then
            print_status "Book '$book_name' inserted successfully (HTTP $status_code) in ${time_total}s"
            
            # Try to extract book ID from response if it's JSON
            if command -v jq >/dev/null 2>&1 && echo "$response_body" | jq . >/dev/null 2>&1; then
                local book_id=$(echo "$response_body" | jq -r '.id // empty' 2>/dev/null)
                if [ -n "$book_id" ]; then
                    echo "   📖 Book ID: $book_id"
                fi
            fi
            
            return 0
        elif [ "$status_code" = "400" ]; then
            print_warning "Book '$book_name' might already exist (HTTP $status_code)"
            echo "   Response: $response_body"
            return 2  # Special return code for "already exists"
        elif [ "$status_code" = "409" ]; then
            print_warning "Book '$book_name' conflicts with existing data (HTTP $status_code)"
            echo "   Response: $response_body"
            return 2
        else
            print_error "Attempt $attempt failed for '$book_name' (HTTP $status_code)"
            echo "   Response: $response_body"
            
            if [ $attempt -lt $MAX_RETRIES ]; then
                print_info "Retrying in $RETRY_DELAY seconds..."
                sleep $RETRY_DELAY
            fi
        fi
        
        attempt=$((attempt + 1))
    done
    
    print_error "Failed to insert '$book_name' after $MAX_RETRIES attempts"
    return 1
}

# Check API availability first
if ! check_api_availability; then
    print_error "Cannot proceed - API is not available"
    echo "Make sure your application is running with: ./docker/start.sh"
    exit 1
fi

# Find all book JSON files in the current directory
book_files=(book*.json)

if [ ${#book_files[@]} -eq 0 ] || [ ! -f "${book_files[0]}" ]; then
    print_error "No book JSON files found (book*.json)"
    print_info "Expected files: book1.json, book2.json, book3.json, book4.json"
    exit 1
fi

echo
print_info "Found ${#book_files[@]} book file(s) to process"
echo

# Initialize counters
successful_inserts=0
failed_inserts=0
already_exists=0

# Process each book file
for json_file in "${book_files[@]}"; do
    echo "----------------------------------------"
    
    result=$(insert_book "$json_file")
    exit_code=$?
    
    case $exit_code in
        0)
            successful_inserts=$((successful_inserts + 1))
            ;;
        1)
            failed_inserts=$((failed_inserts + 1))
            ;;
        2)
            already_exists=$((already_exists + 1))
            ;;
    esac
    
    # Small delay between requests to avoid overwhelming the server
    sleep 0.5
done

# Print summary
echo
echo "========================================"
echo -e "${BLUE}📊 Insertion Summary${NC}"
echo "========================================"
print_status "Successfully inserted: $successful_inserts"
if [ $already_exists -gt 0 ]; then
    print_warning "Already existed/Conflicts: $already_exists"
fi
if [ $failed_inserts -gt 0 ]; then
    print_error "Failed insertions: $failed_inserts"
fi

echo
total_processed=$((successful_inserts + already_exists + failed_inserts))
echo "Total processed: $total_processed/${#book_files[@]}"

# Verify insertions by checking if books exist
if [ $successful_inserts -gt 0 ]; then
    echo
    print_info "Verifying insertions by fetching all books..."
    
    all_books_response=$(curl -s "$API_URL" 2>/dev/null)
    if [ $? -eq 0 ] && command -v jq >/dev/null 2>&1; then
        book_count=$(echo "$all_books_response" | jq '. | length' 2>/dev/null || echo "unknown")
        print_status "Total books in database: $book_count"
    fi
fi

# Exit with appropriate code
if [ $failed_inserts -gt 0 ]; then
    exit 1
elif [ $already_exists -gt 0 ] && [ $successful_inserts -eq 0 ]; then
    print_info "All books already exist in the database"
    exit 0
else
    print_status "Book insertion completed successfully"
    exit 0
fi