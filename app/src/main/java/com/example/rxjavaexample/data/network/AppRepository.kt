package com.example.rxjavaexample.data.network

class AppRepository {

    private val webService by lazy { WebService() }

    fun getCategory() = webService.getCategoryById()

    fun getSubCategoryById(id: Int) = webService.getSubCatById(id)
}