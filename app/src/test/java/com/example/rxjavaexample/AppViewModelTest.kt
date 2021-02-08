package com.example.rxjavaexample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.rxjavaexample.data.model.CatResponse
import com.example.rxjavaexample.data.model.SubCatResponse
import com.example.rxjavaexample.data.network.AppRepository
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AppViewModelTest{

    lateinit var viewModel: AppViewModel
    @Mock
    lateinit var repository: AppRepository
    @Mock
    lateinit var catResponse: CatResponse

    lateinit var subCatResponse: SubCatResponse

    @Mock
    lateinit var observer: Observer<Int>

    @get: Rule
    var instantTask = InstantTaskExecutorRule()

    companion object{
        @JvmStatic
        @BeforeClass
        fun setUpRxSchedulers(){
            val immediate = object : Scheduler(){
                override fun createWorker(): Worker {
                    return ExecutorScheduler.ExecutorWorker(Runnable::run, false)
                }

                override fun scheduleDirect(
                    run: Runnable,
                    delay: Long,
                    unit: TimeUnit
                ): Disposable {
                    return super.scheduleDirect(run, 0, unit)
                }
            }
            RxJavaPlugins.setInitIoSchedulerHandler{ scheduler -> immediate }
            RxJavaPlugins.setInitComputationSchedulerHandler{ scheduler -> immediate }
            RxJavaPlugins.setInitNewThreadSchedulerHandler{ scheduler -> immediate }
            RxJavaPlugins.setInitSingleSchedulerHandler{ scheduler -> immediate }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler{ scheduler -> immediate }
        }
    }

    @Before
    fun setup(){
        viewModel = AppViewModel(repository)
    }

    @Test
    fun getSubCategoryInfo_success_countEqualsOne(){

        whenever(repository.getCategory()).thenReturn(Single.just(catResponse))
        subCatResponse = SubCatResponse(1, listOf(), false)
        whenever(repository.getSubCategoryById(any())).thenReturn(Single.just(subCatResponse))
        //whenever(subCatResponse.count).thenReturn(1)

        assertEquals(1, subCatResponse.count)

        viewModel.getSubCategoryInfo()
        viewModel.data.observeForever(observer)

        verify(repository).getCategory()
        /*verify(repository).getSubCategoryById(any())
        verify(subCatResponse).count*/
        val value = viewModel.data.value
        //verify(observer).onChanged(1)
        assertEquals(1, value)
    }
}