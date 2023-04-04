package com.personal.chatroommobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.data.repositories.AccountRepository
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import com.personal.chatroommobile.data.source.AccountDataSource
import com.personal.chatroommobile.data.source.RelationshipDataSource
import com.personal.chatroommobile.services.SocketHandler
import com.personal.chatroommobile.ui.account.AccountFragment
import com.personal.chatroommobile.ui.account.AccountViewModel
import com.personal.chatroommobile.ui.contacts.ContactFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.main_toolbar)
        val openContactList = toolbar.menu.findItem(R.id.open_contact_list)
        val openUserList = toolbar.menu.findItem(R.id.open_user_list)
        val openMyProfile = toolbar.menu.findItem(R.id.open_my_profile)

        val socket = SocketHandler.getSocket()

        val accountViewModel = AccountViewModel(
            AccountRepository(
                AccountDataSource(CacheData.token)
            ),
            RelationshipRepository(
                RelationshipDataSource(CacheData.token)
            )
        )

        // Load user profile.

        accountViewModel.profile.observe(this, Observer {
            val profile = it ?: return@Observer

            profile.groups.forEach { it_ ->
                socket.invoke("ConnectGroup", it_.id, "MOBILE")
            }

            CacheData.profile = profile
        })

        lifecycleScope.launch {
            accountViewModel.profile()
        }

        // Toolbar actions.

        openContactList.isVisible = false

        toolbar.setNavigationOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                ContactFragment()
            ).commit()

            toolbar.navigationIcon = null

            openContactList.isVisible = false
            openMyProfile.isVisible = true
            openUserList.isVisible = true
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open_contact_list -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                        ContactFragment()
                    ).commit()

                    toolbar.navigationIcon = null

                    openContactList.isVisible = false
                    openMyProfile.isVisible = true
                    openUserList.isVisible = true
                }
                R.id.open_user_list -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                        ContactFragment(true)
                    ).commit()

                    toolbar.setNavigationIcon(R.drawable.ic_back)

                    openUserList.isVisible = false
                    openContactList.isVisible = true
                }
                R.id.open_my_profile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                        AccountFragment()
                    ).commit()

                    toolbar.setNavigationIcon(R.drawable.ic_back)

                    openUserList.isVisible = false
                    openMyProfile.isVisible = false
                    openContactList.isVisible = true
                }
            }
            true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                ContactFragment()
            ).commit()
        }
    }
}