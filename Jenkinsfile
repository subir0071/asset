def readProperties(){
	def properties_file_path = "${workspace}" + "@script/properties.yml"
	def property = readYaml file: properties_file_path

    env.APP_NAME = property.APP_NAME
    env.MS_NAME = property.MS_NAME
    env.BRANCH = property.BRANCH
    env.GIT_SOURCE_URL = property.GIT_SOURCE_URL
    env.SONAR_HOST_URL = property.SONAR_HOST_URL
    
}

def firstTimeDevDeployment(projectName,msName){
    openshift.withCluster() {
        openshift.withProject(projectName) {
            def bcSelector = openshift.selector( "bc", msName)
            def bcExists = bcSelector.exists()
            if (!bcExists) {
                openshift.newApp("redhat-openjdk18-openshift:1.1~${GIT_SOURCE_URL}","--strategy=source")
                def rm = openshift.selector("dc", msName).rollout()
                timeout(15) { 
                  openshift.selector("dc", msName).related('pods').untilEach(1) {
                    return (it.object().status.phase == "Running")
                  }
                }
                openshiftTag(namespace: projectName, srcStream: msName, srcTag: 'latest', destStream: msName, destTag: 'test')
                openshiftTag(namespace: projectName, srcStream: msName, srcTag: 'latest', destStream: msName, destTag: 'prod')
            } else {
                sh 'echo build config already exists in development environment'  
            } 
        }
    }
}

def firstTimeTestDeployment(sourceProjectName,destinationProjectName,msName){
    openshift.withCluster() {
        openshift.withProject(destinationProjectName){
	    def dcSelector = openshift.selector( "dc", msName)
            def dcExists = dcSelector.exists()
	    if(!dcExists){
	    	openshift.newApp(sourceProjectName+"/"+msName+":"+"test")   
	    }
            else {
                sh 'echo deployment config already exists in testing environment'  
            } 
        }
    }
}

def firstTimeProdDeployment(sourceProjectName,destinationProjectName,msName){
    openshift.withCluster() {
        openshift.withProject(destinationProjectName){
	    def dcSelector = openshift.selector( "dc", msName)
            def dcExists = dcSelector.exists()
	    if(!dcExists){
	    	openshift.newApp(sourceProjectName+"/"+msName+":"+"prod")   
	    }
            else {
                sh 'echo deployment config already exists in production environment'  
            } 
        }
    }
}

def buildApp(projectName,msName){
    openshift.withCluster() {
        openshift.withProject(projectName){
            openshift.startBuild(msName,"--wait")   
        }
    }
}

def deployApp(projectName,msName){
    openshift.withCluster() {
        openshift.withProject(projectName){
            openshiftDeploy(namespace: projectName,deploymentConfig: msName)
        }
    }
}

podTemplate(cloud:'openshift',label: 'selenium', 
  containers: [
    containerTemplate(
      name: 'jnlp',
      image: 'cloudbees/jnlp-slave-with-java-build-tools',
      alwaysPullImage: true,
      args: '${computer.jnlpmac} ${computer.name}'
    )])
{
	node 
	{
	   def MAVEN_HOME = tool "Maven_HOME"
	   def JAVA_HOME = tool "JAVA_HOME"
	   env.PATH="${env.PATH}:${MAVEN_HOME}/bin:${JAVA_HOME}/bin"
	   
	   stage('First Time Deployment'){
			readProperties()
			firstTimeDevDeployment("${APP_NAME}-dev", "${MS_NAME}")
			firstTimeTestDeployment("${APP_NAME}-dev", "${APP_NAME}-test", "${MS_NAME}")
			firstTimeProdDeployment("${APP_NAME}-dev", "${APP_NAME}-prod", "${MS_NAME}")
	   }
	   
	   stage('Checkout')
	   {
		   checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: "${GIT_SOURCE_URL}"]]])
	   }

	   stage('Initial Setup')
	   {
		   sh 'mvn clean compile'
	   }
		
	   }
	   stage('Code Quality Analysis')
	   {
		   sh 'mvn sonar:sonar -Dsonar.host.url="${SONAR_HOST_URL}"'
	   }

	   stage('Unit Testing')
	   {
			sh 'mvn test'
	   }

	   stage('Code Coverage')
	   {
		sh 'mvn package'
		jacoco(deltaBranchCoverage: '10', deltaClassCoverage: '10', deltaComplexityCoverage: '10', deltaInstructionCoverage: '10', deltaLineCoverage: '10', deltaMethodCoverage: '20')
	   }

	   stage('Security Scanning')
	   {
		  sh 'mvn spotbugs:check'  // findbugs:findbugs


	   stage('Dev - Build Application')
	   {
		   buildApp("${APP_NAME}-dev", "${MS_NAME}")
	   }

	   stage('Dev - Deploy Application')
	   {
		   deployApp("${APP_NAME}-dev", "${MS_NAME}")
	   }
		
	   /*stage('Jmeter')
	   {
		   sh 'mvn verify'
	   }*/	

	   stage('Tagging Image for Testing')
	   {
		   openshiftTag(namespace: '$APP_NAME-dev', srcStream: '$MS_NAME', srcTag: 'latest', destStream: '$MS_NAME', destTag: 'test')
	   }

	   stage('Test - Deploy Application')
	   {
		   deployApp("${APP_NAME}-test", "${MS_NAME}")
	   }
		
	   node('selenium')
	   {
		stage('Integration Testing')
		{
			container('jnlp')
			{
				 checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: "${GIT_SOURCE_URL}"]]])
			 sh 'mvn integration-test'
			}
		 }
		}
		
		stage('Tagging Image for Testing')
		{
			openshiftTag(namespace: '$APP_NAME-dev', srcStream: '$MS_NAME', srcTag: 'latest', destStream: '$MS_NAME', destTag: 'prod')
		}	
		
		stage('Deploy to Production approval')
		{
		   input "Deploy to Production Environment?"
		}
		
		stage('Prod - Deploy Application')
		{
		   deployApp("${APP_NAME}-prod", "${MS_NAME}")
		}	
	 
	}
}	
