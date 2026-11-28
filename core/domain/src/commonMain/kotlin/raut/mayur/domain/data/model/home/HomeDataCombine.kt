package raut.mayur.domain.data.model.home

import raut.mayur.domain.data.model.home.chart.Chart
import raut.mayur.domain.data.model.mood.Mood
import raut.mayur.domain.utils.Resource

data class HomeDataCombine(
    val home: Resource<Triple<String?, List<HomeItem>, String?>>,
    val mood: Resource<Mood>,
    val chart: Resource<Chart>,
    val newRelease: Resource<List<HomeItem>>,
)