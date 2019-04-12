package de.kramhal.containerlist

import mu.KLogging
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@RestController
class RestApi(private val findRunningWaveInstances: FindRunningWaveInstances) : ErrorController {

    companion object : KLogging()

    override fun getErrorPath() = "/error"

    @RequestMapping(value = ["/error"])
    fun error(request: HttpServletRequest): String {
        val attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        if (attribute != null)
            return attribute.toString()
        return "unknown error"
    }

    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun call(): List<FindRunningWaveInstances.WaveInstance> {
        return findRunningWaveInstances.findRunningWaveInstances()
    }
}
