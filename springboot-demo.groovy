node{   
        stage('Clone repository') {
			//check CODE
			git credentialsId: 'gitlab-admin', url: 'http://172.18.161.164:8888/zhanghc/springboot-demo.git'
        }

        stage('Sonar scan') {
			withSonarQubeEnv {
			 // sh 'mvn sonar:sonar'
			}
        }
        stage('Maven Build') {
            sh "mvn package -Dmaven.test.skip=true docker:build"
        }
        stage('Docker tag'){
        //打tag
        echo "docker tag ......" 
		def pom = readMavenPom file: 'pom.xml'
		def image="172.18.161.165:8888/springcloud/${pom.artifactId}:${pom.version}"
		try{
             sh 'docker rmi -f ${image}'
         }catch(e){
        // err message
        }
			sh "docker tag ${pom.artifactId}:${pom.version} ${image}"
		}
		stage('Docker push'){
    	  //执行推送到仓库
		  echo "docker push ......" 
		  def pom = readMavenPom file: 'pom.xml'
		  def image="172.18.161.165:8888/springcloud/${pom.artifactId}:${pom.version}"
		  withDockerRegistry(credentialsId: 'harbor-login', url: 'http://172.18.161.165:8888') {
			sh "docker push ${image}"
	     }
		}
		stage('Deploy app'){
    	 //发布到K8s环境
		 echo "deploy app ......" 
		 def pom = readMavenPom file: 'pom.xml'
		 sh "sed -i 's/<BUILD_TAG>/${pom.version}/' deployment.yaml"
		 sh "kubectl apply -f deployment.yaml"
       }
}
