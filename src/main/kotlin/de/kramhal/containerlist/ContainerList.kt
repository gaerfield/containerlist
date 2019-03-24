package de.kramhal.containerlist

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class ContainerList

fun main(args: Array<String>) {
    SpringApplication.run(ContainerList::class.java, *args)
}
