pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                script {
                    // Arrêter et supprimer tous les conteneurs en cours d'exécution
                    sh "docker ps -q | xargs -r docker stop"
                    sh "docker ps -aq | xargs -r docker rm"

                    // Construire l'application avec Maven
                    tool 'Maven 3.6.3'
                    sh "mvn clean install -DskipTests"
                }
            }
        }
        stage("Run") {
            steps {
                script {
                    // Construire l'image Docker
                    sh "docker build -t mail_app ."

                    // Démarrer un nouveau conteneur
                    sh """
                        docker run -d \
                        --name mon_application_container \
                        -p 8087:8087 \
                        mon_application_image
                    """
                }
            }
        }
    }
}
