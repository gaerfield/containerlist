package de.kramhal.containerlist

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import mu.KLogging
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@RestController
class RestApi : ErrorController {

    companion object : KLogging()

    override fun getErrorPath() = "/error"

    @RequestMapping(value = ["/error"])
    fun error(request: HttpServletRequest): String {
        val attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        if (attribute != null)
            return attribute.toString()
        return "unknwon error"
    }

    @GetMapping("/call", produces = arrayOf("text/plain"))
    fun call(): String {
        val docker = DefaultDockerClient.fromEnv().build()

        val images = docker.listImages(DockerClient.ListImagesParam.allImages())
        return "blubb\n" + images.map { image -> image.toString() }.joinToString("\n")
    }
}
