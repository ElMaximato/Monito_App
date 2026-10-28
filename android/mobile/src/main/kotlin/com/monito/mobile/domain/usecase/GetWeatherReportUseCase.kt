package com.monito.mobile.domain.usecase

import com.monito.mobile.domain.model.WeatherReport
import com.monito.mobile.domain.repository.WeatherRepository

class GetWeatherReportUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(): WeatherReport = repository.getReport()
}
