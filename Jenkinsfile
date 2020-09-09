#!groovy​

/**
 * configuration parameters
 */

/* project configuration */
String javaVersionId = 'jdk-8' // id that matches the java tool with the java version that should be used set as jenkins property

/* git configuration */
String projectName = 'PowerSystemDataModel' // name of the repository, is case insensitive
String orgName = 'johanneshiry' // name of the github organization
String gitCheckoutUrl = "git@github.com:$orgName/${projectName}.git"
String sshCredentialsId = '19f16959-8a0d-4a60-bd1f-5adb4572b702' // id that matches the ssh credentials to interact with the git set as jenkins property

/* ci configuration */
String sonarqubeProjectKey = 'edu.ie3:PowerSystemDataModel' // sonarqube project key, case-sensitive
String codeCovTokenId = 'psdm-codecov-token' // id that matches the code coverage token set as jenkins property

/* maven central configuration */
String mavenCentralCredentialsId = '87bfb2d4-7613-4816-9fe1-70dfd7e6dec2' // id that matches the maven central credentials set as jenkins property
String mavenCentralSignKeyFileId = 'dc96216c-d20a-48ff-98c0-1c7ba096d08d' // id that matches the maven central sign key file set as jenkins property
String mavenCentralSignKeyId = 'a1357827-1516-4fa2-ab8e-72cdea07a692' // id that matches the maven central sign key id set as jenkins property

/* Rocket.Chat configuration */
String rocketChatChannel = 'jenkins'

/**
 * pipeline configuration
 */

/* setup pipeline properties */
// dev and master need manual deploy capabilities
if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "dev") {
    constantBranchesProps()
} else {
    // all other branches need trigger capabilities for PRs
    temporaryBranchesProps()
}

/**
 * pipeline
 */

node {
    ansiColor('xterm') {
        try {
            // set java version
            setJavaVersion(javaVersionId)

            // set build display name
            currentBuild.displayName = determineDisplayName()

            // notify rocket chat
            String startPipelineMsg = buildStartMsg()
            notifyRocketChat(rocketChatChannel, ':jenkins_triggered:', startPipelineMsg)

            // determine branch name that should be checked out
            net.sf.json.JSONObject prJsonObj = getPRJsonObj(orgName, projectName, env.CHANGE_ID)
            String currentBranchName = prJsonObj == null ? env.BRANCH_NAME : prJsonObj.head.ref
            String targetBranchName = prJsonObj == null ? null : prJsonObj.base.ref
            String branchType = getBranchType(currentBranchName)

            // checkout scm
            String commitHash = ""
            stage('checkout') {
                // commit hash from scm checkout
                // https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/Complete-Jenkins-Git-environment-variables-list-for-batch-jobs-and-shell-script-builds
                commitHash = gitCheckout(projectName, gitCheckoutUrl, currentBranchName, sshCredentialsId).GIT_COMMIT
            }

            // version check
            stage('version check') {
                // version check can only be executed, if target branch is known (derived from a PR)
                if (targetBranchName == "main" || targetBranchName == "dev") {
                    if (checkVersion(currentBranchName, targetBranchName, projectName, projectName, gitCheckoutUrl, sshCredentialsId) != 0)
                        error "Version check failed! See log for version differences."
                } else if (targetBranchName == null) {
                    // if this branch is the dev branch, we can still do version check to compare if dev and master have the same semnatic version
                    if (env.BRANCH_NAME == "dev") {
                        if (checkVersion(currentBranchName, "main", projectName, projectName, gitCheckoutUrl, sshCredentialsId) != 0)
                            error "Version check failed! See log for version differences."
                    } else {
                        println "No PR for branch '$currentBranchName' exists. Cannot check versioning! Please create a PR to enable version check."
                    }
                } else {
                    error "Target branch name '$targetBranchName' for merging is not supported! Please select either 'dev' or 'main' as " +
                            "target branch for merging!"
                }
            }

            if (branchType == "hotfix" || branchType == "release") {
                // release and hotfix branches needs a merge into dev as well, automatically create a draft PR to dev as well
                stage('handle dev PR') {
                    // only create draft PR if a PR has been handed in already, otherwise skip this step
                    if (prJsonObj != null) {
                        GString baseRefTargetRef = "dev,${prJsonObj.head.ref}"

                        println baseRefTargetRef // todo remove debug

                        // get all open pull requests
                        net.sf.json.JSONObject openPRsJsonObj = curlOpenPRs(orgName, projectName)
                        for(item in openPRsJsonObj.items){
                            net.sf.json.JSONObject prObject = getPRJsonObj(orgName, projectName, "${item.number}")
                            if("${prObject.base.ref},${prObject.head.ref}" == baseRefTargetRef){
                                // PR exists
                                println("PR exists!") // todo check if this works
                                break
                            }else{
                                println ("PR does not exist!")
                            }

                        }



                    } else {
                        println "No PR for main branch handed in yet. Not going to create a draft PR for dev branch!"
                    }
                }
            }

            // test the project
            stage('run tests') {
                gradle('--refresh-dependencies clean spotlessCheck pmdMain pmdTest spotbugsMain ' +
                        'spotbugsTest test jacocoTestReport jacocoTestCoverageVerification', projectName)
            }

            // sonarqube analysis
            stage('sonarqube analysis') {
                String sonarqubeCmd = determineSonarqubeGradleCmd(sonarqubeProjectKey, orgName, projectName)
                withSonarQubeEnv() { // will pick the global server connection from jenkins for sonarqube
                    gradle(sonarqubeCmd, projectName)
                }
            }

            // sonarqube quality gate
            stage("quality gate") {
                timeout(time: 1, unit: 'HOURS') {
                    // just in case something goes wrong, pipeline will be killed after a timeout
                    def qg = waitForQualityGate() // reuse taskId previously collected by withSonarQubeEnv
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }

            // deploy stage only if branch is main or dev
            if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "dev") {
                stage('deploy') {
                    // todo JH
                }
            }

            // post processing
            stage('post processing') {
                // publish reports
                publishReports(projectName)

                // call codecov.io
                withCredentials([string(credentialsId: codeCovTokenId, variable: 'codeCovToken')]) {
                    sh "curl -s https://codecov.io/bash | bash -s - -t ${env.codeCovToken} -C ${commitHash}"
                }

                // if this has been a merge of a hotfix or a release additional steps needs to be carried out
                if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "dev") {

                }

                // notify Rocket.Chat
                String successMsg = buildSuccessMsg()
                notifyRocketChat(rocketChatChannel, ':jenkins_party:', successMsg)
            }

        } catch (Exception e) {
            // set build result to failure
            currentBuild.result = 'FAILURE'

            // publish reports even on failure
            publishReports(projectName)

            // print exception
            Date date = new Date()
            println("[ERROR] [${date.format("dd/MM/yyyy")} - ${date.format("HH:mm:ss")}] " + e)

            // notify rocket chat
            net.sf.json.JSONObject prJsonObj = getPRJsonObj(orgName, projectName, env.CHANGE_ID)
            String branchName = prJsonObj == null ? env.BRANCH_NAME : prJsonObj.head.ref
            String errorMsg = "CI failed.\n" +
                    "*project:* ${projectName}\n" +
                    "*branch:* ${branchName}\n" +
                    "*error:* ${e.getMessage()}\n"
            notifyRocketChat(rocketChatChannel, ':jenkins_explode:', errorMsg)
        }

    }
}


/**
 * pipeline stages - all methods require node context
 */

def setJavaVersion(String javaVersionId) {
    env.JAVA_HOME = "${tool javaVersionId}"
    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
}

/* git interaction */

def gitCheckout(String relativeTargetDir, String gitCheckoutUrl, String branch, String sshCredentialsId) {
    checkout([
            $class                           : 'GitSCM',
            branches                         : [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions                       : [[$class: 'RelativeTargetDirectory', relativeTargetDir: relativeTargetDir]],
            submoduleCfg                     : [],
            userRemoteConfigs                : [[credentialsId: sshCredentialsId, url: gitCheckoutUrl]]
    ])
}

/* gradle */

def gradle(String command, String relativeProjectDir) {
    env.JENKINS_NODE_COOKIE = 'dontKillMe' // this is necessary for the Gradle daemon to be kept alive

    // switch directory to be able to use gradle wrapper
    sh(script: """cd $relativeProjectDir""" + ''' set +x; ./gradlew ''' + """$command""", returnStdout: true)
}

def determineSonarqubeGradleCmd(String sonarqubeProjectKey, String orgName, String projectName) {
    switch (env.BRANCH_NAME) {
        case "main":
            return "sonarqube -Dsonar.branch.name=master -Dsonar.projectKey=$sonarqubeProjectKey"
            break
        case "dev":
            return "sonarqube -Dsonar.branch.name=master -Dsonar.projectKey=$sonarqubeProjectKey"
            break
        default:
            String gradleCommand = "sonarqube -Dsonar.projectKey=$sonarqubeProjectKey"
            // if this branch has a PR, the sonarqube cmd needs to be adapted
            if (env.CHANGE_ID == null) {
                // no PR exists
                return gradleCommand + " -Dsonar.branch.name=${env.BRANCH_NAME}"
            } else {
                // PR exists, adapt cmd accordingly
                return gradleCommand + " -Dsonar.pullrequest.branch=${env.BRANCH_NAME} -Dsonar.pullrequest.key=${env.CHANGE_ID} " +
                        "-Dsonar.pullrequest.base=master -Dsonar.pullrequest.github.repository=${orgName}/${projectName} " +
                        "-Dsonar.pullrequest.provider=Github"
            }
            break
    }
}

def determineDisplayName() {
    // todo JH
    String buildVersionNo = currentBuild.displayName // current build default is always the build version no
    String
    if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "dev") {

    } else {

    }

    return "dummyDisplayName"
}

def publishReports(String relativeProjectDir) {
    // publish test reports
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, escapeUnderscores: false, keepAll: true, reportDir: relativeProjectDir + '/build/reports/tests/allTests', reportFiles: 'index.html', reportName: "${relativeProjectDir}_java_tests_report", reportTitles: ''])

    // publish jacoco report for main project only
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, escapeUnderscores: false, keepAll: true, reportDir: relativeProjectDir + '/build/reports/jacoco', reportFiles: 'index.html', reportName: "${relativeProjectDir}_jacoco_report", reportTitles: ''])

    // publish pmd report for main project only
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, escapeUnderscores: false, keepAll: true, reportDir: relativeProjectDir + '/build/reports/pmd', reportFiles: 'main.html', reportName: "${relativeProjectDir}_pmd_report", reportTitles: ''])

    // publish spotbugs report for main project only
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, escapeUnderscores: false, keepAll: true, reportDir: relativeProjectDir + '/build/reports/spotbugs', reportFiles: 'main.html', reportName: "${relativeProjectDir}_spotbugs_report", reportTitles: ''])
}

/* Rocket.Chat */

def notifyRocketChat(String rocketChatChannel, String emoji, String message) {
    rocketSend channel: rocketChatChannel, emoji: emoji,
            message: message
    rawMessage: true
}

def buildSuccessMsg() {
    // todo JH
    return "dummySuccessMsg"
}

def buildStartMsg() {
    // todo JH
    return "dummyStartMsg"
}

/**
 * utility functions - methods that does not require node context
 */

/* properties */

def constantBranchesProps() {
    properties([parameters(
            [string(defaultValue: '', description: '', name: 'deploy', trim: true)]),
                [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
                [$class: 'ThrottleJobProperty', categories: [], limitOneJobWithMatchingParams: false, maxConcurrentPerNode: 0, maxConcurrentTotal: 0, paramsToUseForLimit: '', throttleEnabled: true, throttleOption: 'project']
    ])
}

def temporaryBranchesProps() {
    properties(
            [pipelineTriggers([
                    issueCommentTrigger('.*!test.*')])
            ])
}

/* git interaction */

def getGithubPRJsonObj(String prId, String orgName, String repoName) {
    def jsonObj = readJSON text: curlByPR(prId, orgName, repoName)
    return jsonObj
}

def curlByPR(String prId, String orgName, String repoName) {
    def curlUrl = "curl https://api.github.com/repos/" + orgName + "/" + repoName + "/pulls/" + prId
    String jsonResponseString = sh(script: curlUrl, returnStdout: true)
    return jsonResponseString
}

def curlOpenPRs(String orgName, String repoName) {
    String curlUrl = "curl https://api.github.com/search/issues?q=repo:$orgName/$repoName+is:pr+is:open"
    def jsonObj = readJSON text: sh(script: curlUrl, returnStdout: true)
    return jsonObj
}


def getPRJsonObj(String orgName, String projectName, String changeId) {
    if (changeId == null) {
        return null
    } else {
        // PR exists, curl the api and retrieve target branch
        return getGithubPRJsonObj(changeId, orgName, projectName)
    }
}


def checkVersion(String branchName, String targetBranchName, String relativeGitDir, String projectName, String gitCheckoutUrl, String sshCredentialsId) {
    // get current branch type
    String branchType = getBranchType(branchName)
    if (branchType == null) {
        println "Cannot derive branch type from current branch with name '$branchName'."
        return -1
    }

    // compare the version
    /// save the current version string
    String[] currentVersion = gradle("-q currentVersion", relativeGitDir).toString().split('\\.')

    /// switch to the comparison branch
    gitCheckout(projectName, gitCheckoutUrl, targetBranchName, sshCredentialsId)
    String[] targetBranchVersion = gradle("-q currentVersion", relativeGitDir).toString().split('\\.')

    if (compareVersionParts(branchType, currentVersion, getBranchType(targetBranchName), targetBranchVersion) != 0) {
        // comparison failed
        return -1
    } else {
        // switch back to current branch
        gitCheckout(projectName, gitCheckoutUrl, branchName, sshCredentialsId)
        return 0
    }
}

def compareVersionParts(String sourceBranchType, String[] sourceBranchVersion, String targetBranchType, String[] targetBranchVersion) {

    switch (sourceBranchType) {
        case "hotfix":
            if (targetBranchType == "main") {
                boolean major = sourceBranchVersion[0].toInteger() == targetBranchVersion[0].toInteger()
                boolean minor = sourceBranchVersion[1].toInteger() == targetBranchVersion[1].toInteger()
                boolean patch = (sourceBranchVersion[2].toInteger() == targetBranchVersion[2].toInteger() + 1)

                if (major && minor && patch) {
                    return 0
                } else {
                    println "Hotfix branch versioning is invalid in comparison to master branch versioning. " +
                            "Only masterBranch.patchVersion + 1 is allowed for hotfix branch!\n" +
                            "hotfixVersion: ${sourceBranchVersion[0]}.${sourceBranchVersion[1]}.${sourceBranchVersion[2]}\n" +
                            "masterVersion: ${targetBranchVersion[0]}.${targetBranchVersion[1]}.${targetBranchVersion[2]}"
                    return -1
                }

            } else if (targetBranchType == "dev") {

                boolean major = sourceBranchVersion[0].toInteger() == targetBranchVersion[0].toInteger()
                boolean minor = sourceBranchVersion[1].toInteger() == targetBranchVersion[1].toInteger()
                boolean patch = (sourceBranchVersion[2].toInteger() == 0 && targetBranchVersion[2].toInteger() == 0)

                if (major && minor && patch) {
                    return 0
                } else {
                    println "Hotfix branch versioning is invalid in comparison to dev branch versioning. " +
                            "Major and minor version must be equal and patch version must be 0" +
                            "hotfixVersion: ${sourceBranchVersion[0]}.${sourceBranchVersion[1]}.${sourceBranchVersion[2]}\n" +
                            "devVersion: ${targetBranchVersion[0]}.${targetBranchVersion[1]}.${targetBranchVersion[2]}"
                    return -1
                }

            } else {
                // invalid branch type for hotfix merge
                return -1
            }
            break
        case "feature":
            if (targetBranchType == "dev") {
                // no change in semVer allowed
                boolean major = sourceBranchVersion[0].toInteger() == targetBranchVersion[0].toInteger()
                boolean minor = sourceBranchVersion[1].toInteger() == targetBranchVersion[1].toInteger()
                boolean patch = (sourceBranchVersion[2].toInteger() == 0 && targetBranchVersion[2].toInteger() == 0)

                if (major && minor && patch) {
                    return 0
                } else {
                    println "Feature branch versioning differs from dev branch versioning. This is not allowed!\n" +
                            "featureVersion: ${sourceBranchVersion[0]}.${sourceBranchVersion[1]}.${sourceBranchVersion[2]}\n" +
                            "devVersion: ${targetBranchVersion[0]}.${targetBranchVersion[1]}.${targetBranchVersion[2]}"
                    return -1
                }

            } else {
                // invalid branch type for feature merge
                println "Invalid target branch type '$targetBranchType' for feature branch. Feature branches can only" +
                        "be merged into dev branch!"
                return -1
            }
            break
        case "release":
            if (targetBranchType == "main") {

            } else if (targetBranchType == "dev") {

            } else {
                // invalid branch type for release merge
            }
            // major == major OR major == major + 1, minor == minor OR minor == minor + 1, patch == patch == 0
            break
        case "dev":
            // target branch type can only be main branch
            if (targetBranchType == "main") {
                // only major and minor version parts need to be checked
                Integer targetMajor = targetBranchVersion[0].toInteger()
                Integer targetMinor = targetBranchVersion[1].toInteger()

                Integer sourceMajor = sourceBranchVersion[0].toInteger()
                Integer sourceMinor = sourceBranchVersion[1].toInteger()

                boolean validCheck1 = targetMajor == sourceMajor && targetMinor == sourceMinor + 1
                boolean validCheck2 = targetMajor == sourceMajor + 1 && targetMinor == sourceMinor

                // patch version always needs to be 0
                boolean patchValid = sourceBranchVersion[2] == 0

                if ((validCheck1 || validCheck2) && patchValid) {
                    return 0
                } else {
                    println "Dev branch versioning does not fit to main branch versioning!\n" +
                            "devVersion: ${sourceBranchVersion[0]}.${sourceBranchVersion[1]}.${sourceBranchVersion[2]}\n" +
                            "mainVersion: ${targetBranchVersion[0]}.${targetBranchVersion[1]}.${targetBranchVersion[2]}"
                    return -1
                }
            } else {
                // invalid branch type for dev branch version comparison
                println "Invalid branch type '$targetBranchType' to be compared with dev branch. Dev branch version" +
                        "can only be compared with main branch type!"
                return -1
            }
            break
        default:
            return -1
            break
    }


}

def getBranchType(String branchName) {
    def dev_pattern = ".*dev"
    def release_pattern = ".*rel/.*"
    def feature_pattern = "^\\pL{2}/#\\d+.*"
    def hotfix_pattern = ".*hotfix/\\pL{2}/#\\d+.*"
    def main_pattern = ".*main"
    if (branchName =~ dev_pattern) {
        return "dev"
    } else if (branchName =~ release_pattern) {
        return "release"
    } else if (branchName =~ main_pattern) {
        return "main"
    } else if (branchName =~ feature_pattern) {
        return "feature"
    } else if (branchName =~ hotfix_pattern) {
        return "hotfix"
    } else {
        return null
    }
}

def getCompareBranchName(String branchType) {
    if (branchType == "feature")
        return "dev"
    else
        return "main"
}