pipeline {
    agent any

    environment {
        DEPLOY_BRANCH = 'dev'
        IMAGE_NAME = 'szchen/sms-backend'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        EC2_HOST = credentials('sms-backend-ec2-host')
        DB_PASSWORD = credentials('sms-backend-db-password')
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Jar') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw -Dtest=NameAggregationServiceImplTest test'
            }
        }

        stage('Docker Build and Push') {
            when {
                expression { env.BRANCH_NAME == env.DEPLOY_BRANCH }
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                        docker buildx build --platform linux/amd64 -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest --push .
                    '''
                }
            }
        }

        stage('Deploy to EC2') {
            when {
                expression { env.BRANCH_NAME == env.DEPLOY_BRANCH }
            }
            steps {
                sshagent(credentials: ['sms-backend-ec2-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@$EC2_HOST "
                          docker pull $IMAGE_NAME:latest &&
                          docker stop sms-backend || true &&
                          docker rm sms-backend || true &&
                          docker run -d \
                            --name sms-backend \
                            --network host \
                            -e SERVER_PORT=8080 \
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgresql-sms \
                            -e SPRING_DATASOURCE_USERNAME=sms_user \
                            -e SPRING_DATASOURCE_PASSWORD='$DB_PASSWORD' \
                            -e DOWNSTREAM_URL=http://18.236.231.101:8080/name/aggregation \
                            -e AGGREGATION_SERVICE_NAME=Suzy \
                            $IMAGE_NAME:latest
                        "
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
