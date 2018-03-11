/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.browser

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.duckduckgo.app.browser.BrowserViewModel.Command.NewTab
import com.duckduckgo.app.browser.BrowserViewModel.Command.Refresh
import com.duckduckgo.app.global.SingleLiveEvent
import com.duckduckgo.app.privacy.ui.PrivacyDashboardActivity.Companion.RELOAD_RESULT_CODE
import com.duckduckgo.app.tabs.model.TabDataRepository
import com.duckduckgo.app.tabs.model.TabEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class BrowserViewModel(private val tabRepository: TabDataRepository) : ViewModel() {

    data class ViewState(
        val isFullScreen: Boolean = false,
        val isDesktopBrowsingMode: Boolean = false
    )

    sealed class Command {
        object Refresh : Command()
        class NewTab(val query: String? = null) : Command()
        class Query(val query: String) : Command()
    }

    val viewState: MutableLiveData<ViewState> = MutableLiveData()
    val command: SingleLiveEvent<Command> = SingleLiveEvent()

    init {
        viewState.value = ViewState()
    }

    fun newSearchRequested() {
        command.value = NewTab()
    }

    fun onSharedTextReceived(input: String) {
        command.value = NewTab(input)
    }

    fun onTabsUpdated(tabs: List<TabEntity>?) {
        if (tabs == null || tabs.isEmpty()) {
            command.value = NewTab()
            return
        }
    }

    fun receivedDashboardResult(resultCode: Int) {
        if (resultCode == RELOAD_RESULT_CODE) command.value = Refresh
    }
}



