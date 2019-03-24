package de.kramhal.containerlist

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient.ListContainersParam.withLabel
import com.spotify.docker.client.DockerClient.ListContainersParam.withStatusRunning
import com.spotify.docker.client.messages.Container
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.net.URL

@Controller
class Controller(private val waveProperties: WaveProperties) {
    companion object : KLogging()

    @Component
    data class WaveProperties(
        @Value("\${wave.domain}") val domain: String,
        @Value("\${wave.contextPath}") val contextPath: String
    )

    data class DockerImage(
        val tags: String,
        val id: String,
        val size: String,
        val created: String
    )

    data class WaveInstance(
        val name: String,
        val appContainer: Container,
        val dbContainer: Container,
        val waveProperties: WaveProperties
    ) {
        fun appPort() = appContainer.ports()
            ?.filter { pm -> pm.privatePort() == 8080 }
            ?.map { pm -> pm.publicPort().toString() }
            ?.firstOrNull().orEmpty()

        fun dbPort() = dbContainer.ports()
            ?.filter { pm -> pm.privatePort() == 50000 }
            ?.map { pm -> pm.publicPort().toString() }
            ?.firstOrNull().orEmpty()

        fun appUrl() = "http://${waveProperties.domain}:${appPort()}/${waveProperties.contextPath}"
        fun version() = URL("${appUrl()}/wave/version").readText()
    }

    @GetMapping("/")
    fun home(model: Model, redirectAttributes: RedirectAttributes): String {
        val runningWaveInstances = findRunningWaveInstances()
        logger.info { "Running Wave Instances: $runningWaveInstances" }
        model.addAttribute("waveInstances", runningWaveInstances)
        return "index"
    }

    private fun findRunningWaveInstances(): List<WaveInstance> {

        val docker = DefaultDockerClient.fromEnv().build()
        val waveContainers = docker.listContainers(
            withStatusRunning(), withLabel("de.esag.project", "wave"))
            ?.groupBy { container ->
                container.names()
                    ?.filter { name -> name.contains("(_tomcat_|_db2_)".toRegex()) }
                    ?.map { name -> name.split("_")[0].removePrefix("/") }
                    ?.firstOrNull().orEmpty()
            }

        return waveContainers
            ?.filter { it.value.size == 2 }
            ?.map { e ->
                val firstContainerIsTomcat = e.value[0].names()
                    ?.firstOrNull { name -> name.contains("tomcat", true) }

                if (firstContainerIsTomcat == null)
                    WaveInstance(name = e.key,
                        appContainer = e.value[1],
                        dbContainer = e.value[0],
                        waveProperties = waveProperties)
                else
                    WaveInstance(name = e.key,
                        appContainer = e.value[0],
                        dbContainer = e.value[1],
                        waveProperties = waveProperties)
            }
            ?.toList<WaveInstance>().orEmpty()
    }


    private fun executeAndReport(name: String, function: () -> Any, redirectAttributes: RedirectAttributes) {
        try {
            logger.info { name }
            function()
            logger.info { "$name was successful" }
            messageOk("$name was successful", redirectAttributes)

        } catch (e: Exception) {
            logger.error(e, { "Error during $name" })
            messageError("Error during $name: ${e.message}", redirectAttributes)
        }
    }

    private fun messageOk(message: String, redirectAttributes: RedirectAttributes) {
        redirectAttributes.addFlashAttribute("message", message)
        redirectAttributes.addFlashAttribute("alertClass", "alert-success")
    }

    private fun messageError(message: String, redirectAttributes: RedirectAttributes) {
        redirectAttributes.addFlashAttribute("message", message)
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger")
    }

}
