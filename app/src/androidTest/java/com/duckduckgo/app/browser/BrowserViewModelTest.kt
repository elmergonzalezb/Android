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

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.duckduckgo.app.browser.BrowserViewModel.Command.NewTab
import com.duckduckgo.app.browser.BrowserViewModel.Command.Refresh
import com.duckduckgo.app.global.model.SiteFactory
import com.duckduckgo.app.privacy.store.TermsOfServiceStore
import com.duckduckgo.app.privacy.ui.PrivacyDashboardActivity
import com.duckduckgo.app.tabs.model.TabDataRepository
import com.duckduckgo.app.tabs.db.TabsDao
import com.duckduckgo.app.trackerdetection.model.TrackerNetworks
import com.nhaarman.mockito_kotlin.lastValue
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BrowserViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockCommandObserver: Observer<BrowserViewModel.Command>

    @Mock
    private lateinit var mockTabsDao: TabsDao

    @Mock
    private lateinit var mockTermsOfServiceStore: TermsOfServiceStore

    @Captor
    private lateinit var commandCaptor: ArgumentCaptor<BrowserViewModel.Command>

    private lateinit var testee: BrowserViewModel

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        val siteFactory = SiteFactory(mockTermsOfServiceStore, TrackerNetworks())
        testee = BrowserViewModel(TabDataRepository(mockTabsDao, siteFactory))
        testee.command.observeForever(mockCommandObserver)
    }

    @After
    fun after() {
        testee.command.removeObserver(mockCommandObserver)
    }

    @Test
    fun whenNewSearchRequestedThenNewTabTriggered() {
        testee.newSearchRequested()
        verify(mockCommandObserver).onChanged(commandCaptor.capture())
        assertNotNull(commandCaptor.value)
        assertTrue(commandCaptor.lastValue is NewTab)
    }

    @Test
    fun whenSharedTextReceivedThenNewTabWithQueryTriggered() {
        testee.onSharedTextReceived("example.com")
        verify(mockCommandObserver).onChanged(commandCaptor.capture())
        assertNotNull(commandCaptor.value)
        val lastCommand = commandCaptor.lastValue as NewTab
        assertEquals("example.com", lastCommand.query)
    }

    @Test
    fun whenReloadDashboardResultReceivedThenRefreshTriggered() {
        testee.receivedDashboardResult(PrivacyDashboardActivity.RELOAD_RESULT_CODE)
        verify(mockCommandObserver).onChanged(commandCaptor.capture())
        assertNotNull(commandCaptor.value)
        assertTrue(commandCaptor.value is Refresh)
    }

    @Test
    fun whenUnknownDashboardResultReceivedThenNoCommandTriggered() {
        testee.receivedDashboardResult(1111)
        verify(mockCommandObserver, never()).onChanged(commandCaptor.capture())
    }

}