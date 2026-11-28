package raut.mayur.domain.data.model.home

import raut.mayur.domain.data.model.home.chart.Chart
import raut.mayur.domain.data.model.mood.Mood
import raut.mayur.domain.utils.Resource

data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)