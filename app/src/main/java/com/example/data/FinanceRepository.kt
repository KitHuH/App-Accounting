package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    fun getExpensesForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Expense>> {
        return financeDao.getExpensesForMonth(startOfMonth, endOfMonth)
    }

    fun getTotalSpentForMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?> {
        return financeDao.getTotalSpentForMonth(startOfMonth, endOfMonth)
    }

    suspend fun insertExpense(expense: Expense) {
        financeDao.insertExpense(expense)
    }

    suspend fun deleteExpenseById(id: Int) {
        financeDao.deleteExpenseById(id)
    }

    fun getBudgetForMonth(monthYear: String): Flow<Budget?> {
        return financeDao.getBudgetForMonth(monthYear)
    }

    suspend fun setBudget(budget: Budget) {
        financeDao.setBudget(budget)
    }
}
