package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Budget
import com.example.data.Expense
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())
    }

    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        com.example.ui.theme.AppThemeType.valueOf(
            prefs.getString("app_theme", com.example.ui.theme.AppThemeType.AWAN_WAYANG.name) ?: com.example.ui.theme.AppThemeType.AWAN_WAYANG.name
        )
    )
    val themeMode: StateFlow<com.example.ui.theme.AppThemeType> = _themeMode.asStateFlow()

    fun setTheme(themeType: com.example.ui.theme.AppThemeType) {
        _themeMode.value = themeType
        prefs.edit().putString("app_theme", themeType.name).apply()
    }

    // Keep track of the currently viewed month date
    private val _viewDate = MutableStateFlow(Calendar.getInstance())
    
    val currentMonthYearString: StateFlow<String> = _viewDate.map { 
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(it.time)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    private val _currentMonthYear = _viewDate.map {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.time)
    }

    private val startOfMonthAndEnd = _currentMonthYear.map { getMonthBounds(it) }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = startOfMonthAndEnd.flatMapLatest { bounds ->
        repository.getExpensesForMonth(bounds.first, bounds.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalSpent: StateFlow<Double> = startOfMonthAndEnd.flatMapLatest { bounds ->
        repository.getTotalSpentForMonth(bounds.first, bounds.second)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentBudget: StateFlow<Budget?> = _currentMonthYear.flatMapLatest { monthYear ->
        repository.getBudgetForMonth(monthYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addExpense(amount: Double, description: String, category: String, timestamp: Long) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    description = description,
                    category = category,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpenseById(expense.id)
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudget(
                Budget(
                    monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(_viewDate.value.time),
                    totalBudget = amount
                )
            )
        }
    }
    
    fun previousMonth() {
        val newCal = Calendar.getInstance().apply { 
            timeInMillis = _viewDate.value.timeInMillis
            add(Calendar.MONTH, -1)
        }
        _viewDate.value = newCal
    }
    
    fun nextMonth() {
        val newCal = Calendar.getInstance().apply { 
            timeInMillis = _viewDate.value.timeInMillis
            add(Calendar.MONTH, 1)
        }
        _viewDate.value = newCal
    }

    fun resetData() {
        viewModelScope.launch {
            expenses.value.forEach {
                repository.deleteExpenseById(it.id)
            }
            repository.setBudget(Budget(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(_viewDate.value.time), 0.0))
        }
    }

    private fun getCurrentMonthYearString(): String {
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return format.format(Date())
    }

    private fun getMonthBounds(monthYear: String): Pair<Long, Long> {
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = format.parse(monthYear) ?: Date()
        
        val cal = Calendar.getInstance().apply { time = date }
        
        // Start of month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        // End of month
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
