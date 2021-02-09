package com.example.rxjavaexample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.rxjavaexample.data.model.CatResponse
import com.example.rxjavaexample.data.model.Category
import com.example.rxjavaexample.data.model.SubCatResponse
import com.example.rxjavaexample.data.network.AppRepository
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class AppViewModelTest{

    lateinit var viewModel: AppViewModel
    @Mock
    lateinit var repository: AppRepository
    @Mock
    lateinit var category: Category
    lateinit var catResponse: CatResponse
    lateinit var subCatResponse: SubCatResponse

    @Mock
    lateinit var liveDataObserver: Observer<Int>
    lateinit var testObserver: TestObserver<Int>
    lateinit var exception: Exception

    @get: Rule
    var instantTask = InstantTaskExecutorRule()
    @get: Rule
    var rxJavaRule = RxImmediateSchedulerRule()


    @Before
    fun setup(){
        viewModel = AppViewModel(repository)
        catResponse = CatResponse(listOf(category), false)
        subCatResponse = SubCatResponse(1, listOf(), false)
    }

    @Test
    fun getSubCatInfoObservable_success_emitOne(){
        mockSusccess()
        testObserver = viewModel.getSubCatInfoObservable().test()
        testObserver.assertValue(1)
    }

    @Test
    fun getSubCatInfoObservable_getCategoryFail_throwException(){
        mockGetCategoryFail()
        testObserver = viewModel.getSubCatInfoObservable().test()
        testObserver.assertError(exception)
    }

    @Test
    fun getSubCatInfoObservable_getSubCategoryByIdFail_throwException(){
        mockGetSubCategoryByIdFail()
        testObserver = viewModel.getSubCatInfoObservable().test()
        testObserver.assertError(exception)
    }

    @Test
    fun getSubCategoryInfo_emitOne_subCountIsOne(){
        mockSusccess()

        viewModel.getSubCategoryInfo()
        viewModel.data.observeForever(liveDataObserver)

        verify(repository).getCategory()
        verify(repository).getSubCategoryById(anyInt())
        val value = viewModel.data.value
        verify(liveDataObserver).onChanged(1)
        assertEquals(1, value)
    }

    @Test
    fun getSubCategoryInfo_getCategoryFail_subCountIsNull(){
        mockGetCategoryFail()

        viewModel.getSubCategoryInfo()
        viewModel.data.observeForever(liveDataObserver)

        verify(repository).getCategory()
        verify(repository, never()).getSubCategoryById(anyInt())
        val value = viewModel.data.value
        verify(liveDataObserver, never()).onChanged(anyInt())
        assertEquals(null, value)
    }

    @Test
    fun getSubCategoryInfo_getSubCategoryByIdFail_subCountIsNull(){
        mockGetSubCategoryByIdFail()

        viewModel.getSubCategoryInfo()
        viewModel.data.observeForever(liveDataObserver)

        verify(repository).getCategory()
        verify(repository).getSubCategoryById(anyInt())
        val value = viewModel.data.value
        verify(liveDataObserver, never()).onChanged(anyInt())
        assertEquals(null, value)
    }

    @After
    fun teardown(){
        if(::testObserver.isInitialized)
            testObserver.dispose()
    }

    fun mockSusccess(){
        `when`(repository.getCategory()).thenReturn(Single.just(catResponse))
        `when`(repository.getSubCategoryById(anyInt())).thenReturn(Single.just(subCatResponse))
    }

    fun mockGetCategoryFail(){
        exception = Exception("getCategory fail")
        `when`(repository.getCategory()).thenReturn(Single.error(exception))
       // `when`(repository.getSubCategoryById(anyInt())).thenReturn(Single.just(subCatResponse))
    }

    fun mockGetSubCategoryByIdFail(){
        exception = Exception("getSubCategoryById fail")
        `when`(repository.getCategory()).thenReturn(Single.just(catResponse))
        `when`(repository.getSubCategoryById(anyInt())).thenReturn(Single.error(exception))
    }


    companion object{
        /*@JvmStatic
        @BeforeClass*/
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
}