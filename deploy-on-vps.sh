#!/bin/bash

# deploy-on-vps.sh
# Script to pull latest images and restart services on VPS

# --- Configuration ---
# Tên file Docker Compose (phải khớp với file bạn copy lên VPS)
COMPOSE_FILE="docker-compose.yml" # Hoặc "docker-compose.prod.yml"
# Tên service ứng dụng chính trong file Compose
APP_SERVICE_NAME="app"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

ENV_FILE=".env" # File .env.prod phải tồn tại trên VPS

# --- Functions ---
header() {
    echo -e "\n${YELLOW}===============================================${NC}"
    echo -e "${YELLOW}  VPS Deployment Script (Pull & Restart)     ${NC}"
    echo -e "${YELLOW}===============================================${NC}"
}

check_docker() {
    echo -e "${YELLOW}Checking Docker status...${NC}"
    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}Error: Docker is not running. Please start Docker first.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Docker is running.${NC}"
}

check_env_file() {
    echo -e "${YELLOW}Checking for configuration file '$ENV_FILE'...${NC}"
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}Error: Configuration file '$ENV_FILE' not found on VPS.${NC}"
        echo -e "${RED}Please create '$ENV_FILE' with production environment variables.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Configuration file '$ENV_FILE' found.${NC}"
    export $(grep -v '^#' $ENV_FILE | xargs) # Load biến môi trường để hiển thị thông tin
}

check_compose_file() {
    echo -e "${YELLOW}Checking for Docker Compose file '$COMPOSE_FILE'...${NC}"
    if [ ! -f "$COMPOSE_FILE" ]; then
        echo -e "${RED}Error: Docker Compose file '$COMPOSE_FILE' not found on VPS.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Docker Compose file '$COMPOSE_FILE' found.${NC}"
}

pull_images() {
    echo -e "\n${YELLOW}Pulling latest images (especially for '${APP_SERVICE_NAME}')...${NC}"
    # Chỉ pull image của app, các image khác (mysql, redis) thường ít thay đổi
    # hoặc bạn có thể `docker-compose -f ${COMPOSE_FILE} pull` để pull tất cả
    docker-compose -f "${COMPOSE_FILE}" pull "${APP_SERVICE_NAME}"
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to pull image for '${APP_SERVICE_NAME}'. Check Docker Hub or network.${NC}"
        # Có thể pull tất cả các image nếu muốn:
        # docker-compose -f "${COMPOSE_FILE}" pull
        # if [ $? -ne 0 ]; then
        #     echo -e "${RED}Failed to pull images. Check Docker Hub or network.${NC}"
        #     exit 1
        # fi
    else
        echo -e "${GREEN}Image for '${APP_SERVICE_NAME}' pulled successfully.${NC}"
    fi
}

restart_services() {
    echo -e "\n${YELLOW}Stopping existing services (if any) and starting with new images...${NC}"
    # 'up -d' sẽ tự động tạo lại container nếu image đã thay đổi
    docker-compose -f "${COMPOSE_FILE}" up -d --remove-orphans
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Services started/restarted successfully!${NC}"
        echo -e "${GREEN}--------------------------------------------------${NC}"
        echo -e "${GREEN}Application Access Points:${NC}"
        echo -e "- Backend API: http://$(curl -s ifconfig.me):${SERVER_PORT:-8080}  (hoặc domain của bạn)"
        if [ "${SPRINGDOC_SWAGGER_UI_ENABLED}" != "false" ]; then
            echo -e "- Swagger UI:  http://$(curl -s ifconfig.me):${SERVER_PORT:-8080}${SPRINGDOC_SWAGGER_UI_PATH:-/swagger-ui.html}"
        fi
        echo -e "${GREEN}--------------------------------------------------${NC}"
        echo -e "${YELLOW}To view logs: run 'docker-compose -f ${COMPOSE_FILE} logs -f ${APP_SERVICE_NAME}'${NC}"
    else
        echo -e "${RED}Failed to start/restart services. Check Docker Compose logs.${NC}"
        echo -e "${YELLOW}Tip: Run 'docker-compose -f ${COMPOSE_FILE} up' (without -d) to see detailed logs.${NC}"
    fi
}

stop_services() {
    echo -e "\n${YELLOW}Stopping all services defined in ${COMPOSE_FILE}...${NC}"
    docker-compose -f "${COMPOSE_FILE}" down
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Services stopped successfully.${NC}"
    else
        echo -e "${RED}Failed to stop services cleanly.${NC}"
    fi
}

show_logs() {
    echo -e "\n${YELLOW}Showing logs for service '${APP_SERVICE_NAME}'. Press Ctrl+C to stop.${NC}"
    docker-compose -f "${COMPOSE_FILE}" logs -f "${APP_SERVICE_NAME}"
}

# Main menu
show_menu() {
    header
    echo "1) Update & Restart Application (Pull new image & restart)"
    echo "2) Restart All Services (use existing images)"
    echo "3) Stop All Services"
    echo "4) Show Application Logs"
    echo "5) Exit"
    echo -e "-----------------------------------------------"
}

# --- Main Script Execution on VPS ---
header
check_docker
check_env_file
check_compose_file

# Login vào Docker Hub nếu image của bạn là private (chỉ cần làm một lần sau khi VPS khởi động lại)
# echo -e "${YELLOW}Consider running 'docker login' if your app image is private and you haven't logged in recently.${NC}"

while true; do
    show_menu
    read -r -p "Enter your choice (1-5): " choice

    case $choice in
        1) # Update & Restart
            pull_images
            restart_services
            ;;
        2) # Restart existing
            echo -e "${YELLOW}Restarting all services with existing images...${NC}"
            docker compose -f "${COMPOSE_FILE}" restart
            if [ $? -eq 0 ]; then echo -e "${GREEN}Services restarted.${NC}"; else echo -e "${RED}Restart failed.${NC}"; fi
            ;;
        3) # Stop
            stop_services
            ;;
        4) # Logs
            show_logs
            ;;
        5) # Exit
            echo -e "${GREEN}Exiting script. Goodbye!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option '${choice}'. Please try again.${NC}"
            ;;
    esac
    echo -e "\n${YELLOW}Press Enter to return to menu...${NC}"
    read -r
done