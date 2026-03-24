pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = "your-dockerhub-username"
        IMAGE_NAME      = "${DOCKER_HUB_USER}/springboot-app"
        IMAGE_TAG       = "${GIT_BRANCH.split('/').last()}-${GIT_COMMIT.take(7)}"
        GITOPS_REPO     = "https://github.com/Harshana96/springboot-gitops.git"
    }

    stages {

        stage('Print Info') {
            steps {
                echo "Branch   : ${GIT_BRANCH}"
                echo "Commit   : ${GIT_COMMIT}"
                echo "Image tag: ${IMAGE_TAG}"
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Jar') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker logout
                    """
                }
            }
        }

        stage('Update GitOps Repo') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-credentials',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_PASS'
                )]) {
                    script {
                        def overlay = ""
                        if (GIT_BRANCH == "origin/dev")  overlay = "dev"
                        if (GIT_BRANCH == "origin/qa")   overlay = "qa"
                        if (GIT_BRANCH == "origin/main") overlay = "prod"

                        if (overlay != "") {
                            sh """
                                git clone https://${GIT_USER}:${GIT_PASS}@github.com/Harshana96/springboot-gitops.git
                                cd springboot-gitops
                                sed -i 's|newTag:.*|newTag: ${IMAGE_TAG}|' overlays/${overlay}/kustomization.yaml
                                git config user.email "jenkins@ci.local"
                                git config user.name "Jenkins CI"
                                git add overlays/${overlay}/kustomization.yaml
                                git commit -m "ci: update ${overlay} image tag to ${IMAGE_TAG}"
                                git push
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Done! Image: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed. Check logs above."
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
        }
    }
}