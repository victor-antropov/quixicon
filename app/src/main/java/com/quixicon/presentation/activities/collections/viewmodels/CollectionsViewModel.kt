package com.quixicon.presentation.activities.collections.viewmodels

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.lifecycle.LiveData
import com.quixicon.BuildConfig
import com.quixicon.R
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.presentation.CollectionItem
import com.quixicon.domain.interactors.DeleteCollectionInteractor
import com.quixicon.domain.interactors.GetCardsInteractor
import com.quixicon.domain.interactors.GetCollectionsInteractor
import com.quixicon.domain.interactors.ImportCollectionFromFileInteractor
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import java.util.*
import javax.inject.Inject

class CollectionsViewModel @Inject constructor(
    private val getCollectionsInteractor: GetCollectionsInteractor,
    private val deleteCollectionInteractor: DeleteCollectionInteractor,
    private val getCardsInteractor: GetCardsInteractor,
    private val importCollectionFromFileInteractor: ImportCollectionFromFileInteractor,
    private val preferencesBoundary: PreferencesBoundary,
    val app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getCollectionsInteractor,
        deleteCollectionInteractor,
        getCardsInteractor,
        importCollectionFromFileInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            if (field != value) {
                field = value
                preferencesBoundary.saveConfig(value)
            }
        }

    val language: String = config.languageInterface.value

    private val _getCollectionsLiveData = PendingLiveData<EventArgs<List<CollectionModel>>>()
    val getCollectionsLiveData: LiveData<EventArgs<List<CollectionModel>>> = _getCollectionsLiveData

    private val _getCardDeletedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCardDeletedLiveData: LiveData<EventArgs<Unit>> = _getCardDeletedLiveData

    private val _getCollectionExportDataLiveData = PendingLiveData<EventArgs<ByteArray>>()
    val getCollectionExportDataLiveData: LiveData<EventArgs<ByteArray>> = _getCollectionExportDataLiveData

    private val _importCollectionLiveData = PendingLiveData<EventArgs<Unit>>()
    val importCollectionLiveData: LiveData<EventArgs<Unit>> = _importCollectionLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCollections(order: CollectionSortOrder, filterSubject: LanguageCode? = null, filterStudent: LanguageCode? = null) {
        val params = GetCollectionsInteractor.Params(order, filterSubject, filterStudent)
        getCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCollections, ::onError)
        interactor = getCollectionsInteractor
    }

    private fun onGetCollections(collections: List<CollectionItem>) {
        val lang = config.languageInterface
        val isShowGlobal = config.showGlobalCollections

        val isMultiLanguages = !app.baseContext.resources.getBoolean(R.bool.use_only_language)
        val filter = if (config.useFilter) config.languageSubject else null

        val collectionModels = collections.filter {
            it.superType == null || isShowGlobal
        }.map {
            CollectionModel(
                id = it.id,
                name = when (it.superType) {
                    CardType.WORD -> app.getLocaleStringResource(Locale(lang.value), R.string.all_words)
                    CardType.PHRASE -> app.getLocaleStringResource(Locale(lang.value), R.string.all_phrases)
                    CardType.RULE -> app.getLocaleStringResource(Locale(lang.value), R.string.all_rules)
                    else -> it.name ?: ""
                },
                description = when (it.superType) {
                    CardType.WORD -> app.getLocaleStringResource(Locale(lang.value), R.string.catalog_words_description)
                    CardType.PHRASE -> app.getLocaleStringResource(Locale(lang.value), R.string.catalog_phrases_description)
                    CardType.RULE -> app.getLocaleStringResource(Locale(lang.value), R.string.catalog_rules_description)
                    else -> it.description ?: ""
                },
                superType = it.superType,
                qntPhrases = it.qntPhrases,
                qntRules = it.qntRules,
                qntWords = it.qntWords,
                collectionType = it.collectionType,
                subject = if (it.superType != null) filter else it.codeSubject,
                showSubject = (isMultiLanguages && !config.useFilter) || BuildConfig.DEBUG
            )
        }
        _getCollectionsLiveData.value = EventArgs(collectionModels)
    }

    fun deleteCollection(collectionId: Long) {
        val params = DeleteCollectionInteractor.Params(collectionId)
        deleteCollectionInteractor(params, ::onSubscribe, ::onFinally, ::onDeleteCollection, ::onError)
        interactor = deleteCollectionInteractor
    }

    private fun onDeleteCollection(result: Unit) {
        _getCardDeletedLiveData.value = EventArgs(result)
    }

    fun getExportData(id: Long) {
        val params = GetCardsInteractor.Params(id, order = CardSortOrder.DEFAULT)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCards, ::onError)
        interactor = getCardsInteractor
    }

    private fun onGetCards(cards: List<Card>) {
        var str = ""
        cards.forEach {
            str += it.name ?: ""
            str += String.format("\t%s", it.translation ?: "")
            str += String.format("\t%s", it.transcription ?: "")
            str += String.format("\t%s", it.extraData?.description ?: "")
            str += String.format("\t%d\r\n", it.cardType.value)
        }
        _getCollectionExportDataLiveData.value = EventArgs(str.toByteArray())
    }

    fun importCollection(uri: Uri, template: String, filter: LanguageCode? = null) {
        val params = ImportCollectionFromFileInteractor.Params(uri, template, filter)
        importCollectionFromFileInteractor(params, ::onSubscribe, ::onFinally, ::onCollectionImported, ::onError)
        interactor = importCollectionFromFileInteractor
    }

    fun onCollectionImported(id: Long) {
        _importCollectionLiveData.value = EventArgs(Unit)
    }

    fun Context.getLocaleStringResource(
        requestedLocale: Locale?,
        resourceId: Int
    ): String {
        val result: String
        val config = Configuration(resources.configuration)
        config.setLocale(requestedLocale)
        result = createConfigurationContext(config).getText(resourceId).toString()
        return result
    }
}
