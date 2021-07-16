package com.app.easyleasy.dataclasses.generics

import com.fasterxml.jackson.annotation.JsonProperty

import java.util.ArrayList

/**
 *Generic response for data if API return json array
 * */
class TAListResponse<T> : TABaseResponse() {
    @JsonProperty("data")
    var data: ArrayList<T>? = null
}
