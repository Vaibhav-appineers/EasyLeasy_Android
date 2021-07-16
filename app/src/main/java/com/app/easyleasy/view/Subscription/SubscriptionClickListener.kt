package com.app.easyleasy.view.Subscription

import com.app.easyleasy.dataclasses.SubscriptionPlan


interface SubscriptionClickListener {
    fun onSubscriptionClick(data: SubscriptionPlan)
}