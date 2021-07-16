package com.app.easyleasy.dagger.components

import com.app.easyleasy.dagger.FragmentScope
import com.app.easyleasy.dagger.modules.FragmentModule
import com.app.easyleasy.view.friends.FriendsFragment
import com.app.easyleasy.view.home.HomeFragment
import com.app.easyleasy.view.message.MessagesFragment
import com.app.easyleasy.view.profile.ProfileFragment
import com.app.easyleasy.view.settings.SettingsFragment
import dagger.Component

@FragmentScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [FragmentModule::class]
)
interface FragmentComponent{
    fun inject(settingsFragment: SettingsFragment)
    fun inject(profileFragment:ProfileFragment)
    fun inject(homeFragment: HomeFragment)
    fun inject(friendsFragment: FriendsFragment)
    fun inject(messagesFragment: MessagesFragment)
}