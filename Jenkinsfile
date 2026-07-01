pipeline {
    agent any

    environment {
        DEPLOY_BRANCH = 'dev'
        IMAGE_NAME = 'szchen/sms-backend'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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

        stage('Load .env') {
            steps {
                script {
                    if (!fileExists('.env')) {
                        error('.env file not found at repo root')
                    }
                    def envFile = readFile('.env').trim()
                    envFile.split('\n').each { line ->
                        line = line.trim()
                        if (line && !line.startsWith('#') && line.contains('=')) {
                            def idx = line.indexOf('=')
                            def key = line.substring(0, idx).trim()
                            def value = line.substring(idx + 1).trim()
                            env[key] = value
                        }
                    }
                    if (!env.EC2_HOST) {
                        error('EC2_HOST not set in .env')
                    }
                    echo "Loaded EC2_HOST=${env.EC2_HOST}"
                }
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
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} "
                          docker stop sms-backend || true &&
                          docker rm sms-backend || true &&
                          docker pull ${IMAGE_NAME}:latest &&
                          docker run -d \
                            --name sms-backend \
                            --network host \
                            -e SERVER_PORT=8080 \
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgresql-sms \
                            -e SPRING_DATASOURCE_USERNAME=sms_user \
                            -e SPRING_DATASOURCE_PASSWORD=1234 \
                            -e DOWNSTREAM_URL=http://18.237.192.113:8080/name/aggregation \
                            -e AGGREGATION_SERVICE_NAME=Suzy \
                            ${IMAGE_NAME}:latest
                        "
                    '''
                }
            }
        }

        stage('Docker Logout') {
            when {
                expression { env.BRANCH_NAME == env.DEPLOY_BRANCH }
            }
            steps {
                sh 'docker logout || true'
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Build and deploy succeeded.'
        }
        failure {
            echo 'Build or deploy failed.'
        }
    }
}
