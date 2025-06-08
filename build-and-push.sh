#!/bin/bash

# build-and-push.sh
# Script to build and push Docker image to Docker Hub

# --- Configuration ---
DOCKERHUB_USERNAME="dnh2004"
IMAGE_NAME="schoolhealth-app"
VERSION_TAG="latest"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- Functions ---

# XÓA BỎ ĐỊNH NGHĨA check_docker_login() GỐC Ở ĐÂY

check_docker_login() {
    echo -e "${YELLOW}Attempting to ensure Docker login...${NC}"
    echo -e "DEBUG: DOCKERHUB_USERNAME in script is: '${DOCKERHUB_USERNAME}'"

    # Thử login trực tiếp, Docker sẽ sử dụng credentials hiện có nếu hợp lệ,
    # hoặc yêu cầu nhập nếu không có hoặc không hợp lệ.
    # Docker login sẽ trả về mã lỗi khác 0 nếu thất bại.
    if ! docker login -u "${DOCKERHUB_USERNAME}"; then
        # Nếu docker login -u USERNAME thất bại, có thể là do password sai hoặc cần nhập password
        # Hãy thử docker login không có -u để nó tự hỏi username/password
        echo -e "${YELLOW}Initial login attempt with username '${DOCKERHUB_USERNAME}' might have failed or requires password.${NC}"
        echo -e "${YELLOW}Attempting interactive login...${NC}"
        if ! docker login; then
            echo -e "${RED}Docker login failed. Please check your credentials and try again.${NC}"
            exit 1
        fi
    fi

    # Sau khi docker login thành công (dù là tự động hay tương tác),
    # chúng ta có thể kiểm tra lại xem có đúng user không nếu Docker Desktop có cơ chế riêng.
    # Tuy nhiên, trên nhiều hệ thống, `docker info` không hiển thị username.
    # Vì vậy, chúng ta sẽ tin tưởng vào kết quả của `docker login`.
    # Nếu muốn chắc chắn hơn, có thể thử push một image test nhỏ (nhưng phức tạp hơn).

    # Tạm thời, nếu docker login ở trên không báo lỗi, chúng ta coi là thành công.
    echo -e "${GREEN}Docker login process completed. Assuming login as '${DOCKERHUB_USERNAME}' or successful interactive login.${NC}"

    # Bỏ qua việc kiểm tra username từ `docker info` vì nó không đáng tin cậy trên hệ thống của bạn.
    # local docker_info_output
    # docker_info_output=$(docker info)
    # echo -e "DEBUG: Docker Info Output (for reference):\n${docker_info_output}"

    # if ! echo "${docker_info_output}" | grep -q "Username: ${DOCKERHUB_USERNAME}"; then
    #      # Thử lại một lần nữa với login tương tác đầy đủ
    #      echo -e "${YELLOW}Could not confirm username '${DOCKERHUB_USERNAME}' from docker info. Attempting full interactive login...${NC}"
    #      if ! docker login; then
    #          echo -e "${RED}Full interactive Docker login failed. Exiting.${NC}"
    #          exit 1
    #      fi
    #      # Kiểm tra lại lần cuối (có thể vẫn không có username trong info)
    #      docker_info_output=$(docker info)
    #      if ! echo "${docker_info_output}" | grep -q "Username: ${DOCKERHUB_USERNAME}"; then
    #         echo -e "${YELLOW}Warning: Still cannot confirm username '${DOCKERHUB_USERNAME}' from docker info, but proceeding as login reported success.${NC}"
    #      else
    #         echo -e "${GREEN}Confirmed login as '${DOCKERHUB_USERNAME}' after interactive login.${NC}"
    #      fi
    # else
    #    echo -e "${GREEN}Confirmed login as '${DOCKERHUB_USERNAME}'.${NC}"
    # fi
}

build_image() {
    echo -e "\n${YELLOW}Building Docker image: ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG}...${NC}"
    docker build -t "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG}" .
    if [ $? -ne 0 ]; then
        echo -e "${RED}Docker build failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}Docker image built successfully: ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG}${NC}"

    if [ "$VERSION_TAG" != "latest" ]; then
        echo -e "${YELLOW}Also tagging as ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest...${NC}"
        docker tag "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG}" "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest"
    fi
}

push_image() {
    echo -e "\n${YELLOW}Pushing image ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG} to Docker Hub...${NC}"
    docker push "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG}"
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to push ${VERSION_TAG}!${NC}"
        exit 1
    fi
    echo -e "${GREEN}Image ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG} pushed successfully.${NC}"

    if [ "$VERSION_TAG" != "latest" ]; then
        echo -e "${YELLOW}Pushing image ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest to Docker Hub...${NC}"
        docker push "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest"
        if [ $? -ne 0 ]; then
            echo -e "${RED}Failed to push latest tag!${NC}"
        else
            echo -e "${GREEN}Image ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest pushed successfully.${NC}"
        fi
    fi
}

# --- Main Script ---
echo -e "${GREEN}=== Docker Image Build and Push Script ===${NC}"

# 1. Kiểm tra đăng nhập Docker Hub
check_docker_login # Lời gọi này giờ sẽ chạy phiên bản có debug ở trên

# 2. Build Docker image
build_image

# 3. Push Docker image
push_image

echo -e "\n${GREEN}All done! Image ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${VERSION_TAG} (and :latest) should be available on Docker Hub.${NC}"

# XÓA BỎ ĐỊNH NGHĨA check_docker_login() THỨ HAI Ở ĐÂY (NẾU BẠN DI CHUYỂN PHIÊN BẢN DEBUG LÊN TRÊN)
# Hoặc đơn giản là đảm bảo chỉ có MỘT định nghĩa của check_docker_login() trong toàn bộ file.