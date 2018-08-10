node{
    stage('get clone'){
        //check CODE
       git credentialsId: 'github', url: 'https://github.com/zhanghaichang/jenkins-demo.git'
    }

    //定义mvn环境
    def mvnHome = tool 'M3'
    env.PATH = "${mvnHome}/bin:${env.PATH}"

    stage('mvn test'){
        //mvn 测试
        sh "mvn test"
    }

    stage('mvn build'){
        //mvn构建
        sh "mvn clean install -Dmaven.test.skip=true"
    }

    stage('docker build'){
        //执行推送
        echo "docker build ......" 
		def image="${name}/${application}:${tag}"
        sh "docker build -t ${image} ."
	}
    stage('docker deploy'){
        //执行部署脚本
        echo "docker deploy ......" 
	    def image="${name}/${application}:${tag}"
        try{
             sh 'docker rm -f ${application}'
         }catch(e){
        // err message
        }
        docker.image(image).run("-p 8888:9090 --name ${application}")
    }
}
