pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: git
      image: "alpine/git:latest"
      tty: true
      command: ["tail", "-f", "/dev/null"]
      imagePullPolicy: Always
'''
        }
    }
    
    parameters {
        choice(name: 'REPO_NAME', 
               choices: ['oc-terraform-aws-vpc', 'oc-terraform-aws-eks', 'oc-terraform-aws-s3', 'oc-terraform-aws-dns'],
               description: 'Repository name to tag')
        string(name: 'REPO_BRANCH', 
               defaultValue: 'main', 
               description: 'Branch to tag')
        choice(name: 'RELEASE_TYPE', 
               choices: ['patch', 'minor', 'major'], 
               description: 'Version increment type')
        booleanParam(name: 'PUSH_TAG', 
                    defaultValue: true, 
                    description: 'Push tag to remote')
        string(name: 'CREDENTIALS_ID', 
               defaultValue: 'XXX', 
               description: 'Jenkins credentials ID')
        string(name: 'GIT_USER_NAME', 
               defaultValue: 'Jenkins CI/CD', 
               description: 'Git user name')
        string(name: 'GIT_USER_EMAIL', 
               defaultValue: 'xxxxx', 
               description: 'Git user email')
    }
    
    environment {
        BASE_URL = 'github.xxxxxx' // from master repo
    }
    
    stages {
        stage('Setup') {
            steps {
                container('git') {
                    script {
                        // Construct the full repository URL
                        env.REPO_URL = "${env.BASE_URL}/${params.REPO_NAME}.git"
                        echo "Selected repository: ${env.REPO_URL}"
                    }
                    
                    sh "git config --global --add safe.directory '*'"
                    sh "git config --global user.name '${params.GIT_USER_NAME}'"
                    sh "git config --global user.email '${params.GIT_USER_EMAIL}'"
                    
                    withCredentials([usernamePassword(credentialsId: params.CREDENTIALS_ID, 
                                                     usernameVariable: 'GIT_USERNAME', 
                                                     passwordVariable: 'GIT_PASSWORD')]) {
                        sh """
                            rm -rf .git
                            git init
                            git remote add origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@${env.REPO_URL}
                            git fetch origin
                            git checkout -f origin/${params.REPO_BRANCH}
                            git checkout -b ${params.REPO_BRANCH}
                            git fetch --tags
                        """
                    }
                }
            }
        }
        
        stage('Calculate Version') {
            steps {
                container('git') {
                    script {
                        def latestTag = sh(
                            script: 'git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0"', 
                            returnStdout: true
                        ).trim()
                        
                        def version = latestTag.replaceFirst('v', '').tokenize('.')
                        def major = version[0].toInteger()
                        def minor = version[1].toInteger()
                        def patch = version[2].toInteger()
                        
                        switch(params.RELEASE_TYPE) {
                            case 'major': major++; minor = 0; patch = 0; break
                            case 'minor': minor++; patch = 0; break
                            case 'patch': patch++; break
                        }
                        
                        env.NEW_VERSION = "v${major}.${minor}.${patch}"
                        echo "New version: ${env.NEW_VERSION}"
                    }
                }
            }
        }
        
        stage('Create Tag') {
            steps {
                container('git') {
                    sh "git tag -a ${env.NEW_VERSION} -m 'Release ${env.NEW_VERSION}'"
                    
                    script {
                        if (params.PUSH_TAG) {
                            withCredentials([usernamePassword(
                                credentialsId: params.CREDENTIALS_ID, 
                                usernameVariable: 'GIT_USERNAME', 
                                passwordVariable: 'GIT_PASSWORD'
                            )]) {
                                sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${env.REPO_URL} ${env.NEW_VERSION}"
                                echo "Tag ${env.NEW_VERSION} pushed to ${params.REPO_NAME}"
                            }
                        } else {
                            echo "Tag ${env.NEW_VERSION} created locally for ${params.REPO_NAME} (not pushed)"
                        }
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ Successfully tagged ${params.REPO_NAME} with ${env.NEW_VERSION}"
        }
        failure {
            echo "❌ Failed to tag ${params.REPO_NAME}"
        }
    }
}
