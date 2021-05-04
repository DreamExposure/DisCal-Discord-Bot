package org.dreamexposure.discal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration

@SpringBootApplication(exclude = [SessionAutoConfiguration::class])
class Application
