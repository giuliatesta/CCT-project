# Use an official Maven image as a base image
FROM maven:3-eclipse-temurin-21-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Run the Maven Wrapper to build the project
RUN mvn clean install

# Expose the port on which the Spring Boot application will run
EXPOSE 8080

# Command to run the Spring Boot application
CMD ["java", "-jar", "target/cct-project-0.0.1-SNAPSHOT.jar"]
