FROM eclipse-temurin:25-jdk

# Install LLVM Clang 17
RUN apt-get update -qq && \
    apt-get install -y -qq llvm-17 libclang-17-dev && \
    rm -rf /var/lib/apt/lists/*

# Set LD_LIBRARY_PATH
ENV LD_LIBRARY_PATH=/usr/lib/llvm-17/lib:${LD_LIBRARY_PATH}

WORKDIR /workspace
