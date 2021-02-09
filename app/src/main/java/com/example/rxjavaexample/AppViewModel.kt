package com.example.rxjavaexample

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rxjavaexample.data.network.AppRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class AppViewModel(val repository: AppRepository) : ViewModel() {

    //val repository by lazy { AppRepository() }
    val subCatCount by lazy { MutableLiveData<Int>() }
    val data: LiveData<Int>
        get() = subCatCount

    val disposables = CompositeDisposable()

    /*init {
        getSubCategoryInfo()
    }*/

    fun getSubCategoryInfo() {
        disposables.add(
            getSubCatInfoObservable()
                .subscribeWith(object : DisposableSingleObserver<Int>() {
                    override fun onSuccess(t: Int) {
                        subCatCount.value = t
                    }

                    override fun onError(e: Throwable) {
                        //subCatCount.value = -1
                        //Log.d("abc", "onError(): ${e.message}")  this makes unit test fails since Log is not mocked
                    }
                })
        )
    }

    fun getSubCatInfoObservable() = repository.getCategory()
        .subscribeOn(Schedulers.io())
        .flatMap { catResponse ->
            repository.getSubCategoryById(catResponse.data[0].catId)
        }
        .map { subResponse -> subResponse.count }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}

class MyViewModelFactory(val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppViewModel(repository) as T
    }

}