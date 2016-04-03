import jenkins.model.*
import hudson.model.*

Jenkins.instance.setNumExecutors(1)
Jenkins.instance.setMode(Node.Mode.EXCLUSIVE)
Jenkins.instance.save() 
jlc = JenkinsLocationConfiguration.get()
jlc.setUrl("http://" + java.net.InetAddress.localHost.getHostAddress() + ":8080/") 
jlc.save() 


def seedJob = Jenkins.instance.items.findAll { job ->
    job.name =~ ".*seed.*"
}

seedJob.each { job ->
    job.scheduleBuild(new Cause.UserIdCause())
}
