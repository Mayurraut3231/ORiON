package raut.mayur.data.repository

import raut.mayur.domain.data.model.update.UpdateData
import raut.mayur.domain.repository.UpdateRepository
import raut.mayur.domain.utils.Resource
import raut.mayur.kotlinytmusicscraper.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class UpdateRepositoryImpl(
    private val youTube: YouTube,
) : UpdateRepository {
    override fun checkForGithubReleaseUpdate(): Flow<Resource<UpdateData>> =
        flow {
            youTube
                .checkForGithubReleaseUpdate()
                .onSuccess { response ->
                    val apkAsset = response.assets?.filterNotNull()?.firstOrNull {
                        it.name?.endsWith(".apk", ignoreCase = true) == true
                    } ?: response.assets?.filterNotNull()?.firstOrNull()

                    val downloadUrl = apkAsset?.browserDownloadUrl
                        ?: "https://github.com/Mayurraut3231/ORiON/releases/download/${response.tagName ?: ""}/ORiON-${response.tagName ?: ""}.apk"

                    emit(
                        Resource.Success(
                            UpdateData(
                                tagName = response.tagName ?: "",
                                releaseTime = response.publishedAt ?: "",
                                body = response.body ?: "",
                                downloadUrl = downloadUrl,
                                apkSize = apkAsset?.size?.toLong(),
                            ),
                        ),
                    )
                }.onFailure {
                    emit(Resource.Error<UpdateData>(it.localizedMessage ?: "Unknown error"))
                }
        }.flowOn(Dispatchers.IO)

    override fun checkForFdroidUpdate(): Flow<Resource<UpdateData>> =
        flow {
            youTube
                .checkForFdroidUpdate()
                .onSuccess { response ->
                    val latestVersion = response.packages.maxBy { it.versionCode }
                    emit(
                        Resource.Success(
                            UpdateData(
                                tagName = latestVersion.versionName,
                                releaseTime = null,
                                body =
                                    $$"""
                                    ### Update via F-Droid, changelogs: 
                                    - https://github.com/maxrave-dev/SimpMusic/blob/dev/fastlane/metadata/android/en-US/changelogs/$${latestVersion.versionCode}.txt
                                    """.trimIndent(),
                            ),
                        ),
                    )
                }.onFailure {
                    emit(Resource.Error<UpdateData>(it.localizedMessage ?: "Unknown error"))
                }
        }.flowOn(Dispatchers.IO)

    override fun getFdroidSigningKeys(): Flow<Resource<List<String>>> =
        flow {
            youTube
                .getFdroidSigningKeys()
                .onSuccess { emit(Resource.Success(it)) }
                .onFailure { emit(Resource.Error<List<String>>(it.localizedMessage ?: "Unknown error")) }
        }.flowOn(Dispatchers.IO)
}