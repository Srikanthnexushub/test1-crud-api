# ============================================
# STAGE 1: Build Stage
# ============================================
# This stage compiles your Java code and creates the JAR file

# FROM: Specifies the base image to use
# maven:3.9-eclipse-temurin-17 = Maven 3.9 + Java 17 (official image)
FROM maven:3.9-eclipse-temurin-17 AS build

# WORKDIR: Sets the working directory inside the container
# All subsequent commands run from this directory
WORKDIR /app

# COPY: Copies files from your machine into the container
# Copy pom.xml first (for dependency caching - explained below)
COPY pom.xml .

# RUN: Executes a command inside the container during build
# Download all Maven dependencies (this layer is cached)
# -B = batch mode (non-interactive)
# If pom.xml doesn't change, Docker reuses this cached layer (faster builds)
RUN mvn dependency:go-offline -B

# COPY: Now copy all source code
# . (first dot) = everything from current directory on your machine
# . (second dot) = copy to current WORKDIR (/app) in container
COPY src ./src

# RUN: Build the application
# -DskipTests = skip running tests during Docker build (tests already passed)
# clean = remove old builds
# package = compile code and create JAR file
# This creates target/Crud_Operation-1.0-SNAPSHOT.jar
RUN mvn clean package -DskipTests

# ============================================
# STAGE 2: Runtime Stage (Multi-stage build)
# ============================================
# This stage creates the final lightweight image

# Start fresh with just Java runtime (no Maven needed)
# eclipse-temurin:17-jre = Java 17 Runtime Environment only
# JRE is smaller than JDK (JDK = development tools, JRE = runtime only)
FROM eclipse-temurin:17-jre

# WORKDIR: Set working directory in runtime container
WORKDIR /app

# COPY: Copy the JAR file from the build stage
# --from=build = copy from the "build" stage we named above
# /app/target/*.jar = the JAR file Maven created
# app.jar = rename it to app.jar (simpler name)
COPY --from=build /app/target/*.jar app.jar

# EXPOSE: Document which port the app uses
# This is just documentation - doesn't actually open the port
# Your Spring Boot app runs on port 8080 (from application.properties)
EXPOSE 8080

# ENTRYPOINT: The command that runs when container starts
# java -jar app.jar = run your Spring Boot application
# This is like running "mvn spring-boot:run" but using the JAR directly
ENTRYPOINT ["java", "-jar", "app.jar"]

# Why Multi-stage Build?
# - Build stage: ~700 MB (includes Maven, build tools)
# - Runtime stage: ~300 MB (only Java runtime + your JAR)
# - Final image is smaller, faster, more secure
