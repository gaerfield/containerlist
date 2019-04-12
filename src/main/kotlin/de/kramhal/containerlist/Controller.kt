package de.kramhal.containerlist

import mu.KLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class Controller(private val findRunningWaveInstances: FindRunningWaveInstances) {
    companion object : KLogging()


    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun home(model: Model, redirectAttributes: RedirectAttributes): String {
        val runningWaveInstances = findRunningWaveInstances.findRunningWaveInstances()
        logger.info { "Running Wave Instances: $runningWaveInstances" }
        model.addAttribute("waveInstances", runningWaveInstances)
        return "index"
    }

}
