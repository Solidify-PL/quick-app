@Library('CIDD') _


genericBuildPipeline {

    useTemplate('rzm-lib-mvnw') {
        properties {
            space = 'quickapp'
            validate = [
                checkstyle: [
                    enabled: true
                ]
            ]
            _package = [
                agentImage: 'ghcr.io/graalvm/native-image:ol8-java11-22.2.0'
            ]
        }
    }

    stages {
        _package {
            dockerBuild {
                imageName 'quickapp/qa'
                dockerfileLocation './src/main/docker/Dockerfile'
                contextDir './target'
            }
        }

        install {
            dockerPush {
                imageName 'quickapp/qa'
                registry 'docker-releases.internal.solidify.pl'
            }
        }
    }
}
