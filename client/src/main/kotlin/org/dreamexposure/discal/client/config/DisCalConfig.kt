package org.dreamexposure.discal.client.config

import org.dreamexposure.discal.core.`object`.Wizard
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.`object`.event.PreEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DisCalConfig {
    @Bean
    fun calendarWizard(): Wizard<PreCalendar> = Wizard()

    @Bean
    fun eventWizard(): Wizard<PreEvent> = Wizard()
}
