package de.kramhal.containerlist

import com.fasterxml.jackson.annotation.JsonGetter
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.Container
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

@Component
class FindRunningWaveInstances(private val waveProperties: WaveProperties) {

    fun findRunningWaveInstances(): List<WaveInstance> {
        val docker = DefaultDockerClient.fromEnv().build()
        val waveContainers = docker.listContainers(
            DockerClient.ListContainersParam.withStatusRunning(), DockerClient.ListContainersParam.withLabel("de.esag.project", "wave"))
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
            ?.toList().orEmpty()
    }

    data class WaveInstance(
        val name: String,
        val appContainer: Container,
        val dbContainer: Container,
        val waveProperties: WaveProperties
    ) {
        @JsonGetter("appPort")
        fun appPort() = appContainer.ports()
            ?.filter { pm -> pm.privatePort() == 8080 }
            ?.map { pm -> pm.publicPort().toString() }
            ?.firstOrNull().orEmpty()

        @JsonGetter("dbPort")
        fun dbPort() = dbContainer.ports()
            ?.filter { pm -> pm.privatePort() == 50000 }
            ?.map { pm -> pm.publicPort().toString() }
            ?.firstOrNull().orEmpty()

        @JsonGetter("appUrl")
        fun appUrl() = "http://${waveProperties.domain}:${appPort()}/${waveProperties.contextPath}"

        @JsonGetter("version")
        fun version() = URL("${appUrl()}/wave/version").readText()
    }



    @Component
    data class WaveProperties(
        @Value("\${wave.domain}") val domain: String,
        @Value("\${wave.contextPath}") val contextPath: String
    )
}
