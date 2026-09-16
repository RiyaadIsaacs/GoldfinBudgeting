package com.example.goldfinbudgeting.data

import com.example.goldfinbudgeting.Envelope
import com.example.goldfinbudgeting.Expense

// EntityMappers converts between Room table rows and the UI model classes
object EntityMappers {

    // Room expense row -> UI Expense model used in lists and edit screens
    fun toExpense(entity: ExpenseEntity): Expense {
        return Expense(
            name = entity.name,
            amount = entity.amount,

            // Entity field is categoryName. UI model field is category
            category = entity.categoryName,
            dateCreated = entity.dateCreated,
            description = entity.description,
            startTime = entity.startTime,
            endTime = entity.endTime,
            receiptPath = entity.receiptPath,

            // Keep the database id so update / delete can target the correct row
            id = entity.id
        )
    }

    // UI Expense model
    // Room expense row ready to insert or update
    fun toExpenseEntity(expense: Expense): ExpenseEntity {
        return ExpenseEntity(
            id = expense.id,
            name = expense.name,
            amount = expense.amount,
            categoryName = expense.category,
            dateCreated = expense.dateCreated,
            description = expense.description,
            startTime = expense.startTime,
            endTime = expense.endTime,
            receiptPath = expense.receiptPath
        )
    }

    // Room category row + calculated spent amount 
    // spent is passed in because it is not stored in the categories table
    fun toEnvelope(entity: CategoryEntity, spent: Double): Envelope {
        return Envelope(
            name = entity.name,
            min = entity.minGoal,
            max = entity.maxGoal,
            spent = spent,
            dateCreated = entity.dateCreated,
            id = entity.id
        )
    }

    // UI Envelope model - Room category row 
    fun toCategoryEntity(envelope: Envelope): CategoryEntity {
        return CategoryEntity(
            id = envelope.id,
            name = envelope.name,
            minGoal = envelope.min,
            maxGoal = envelope.max,
            dateCreated = envelope.dateCreated
        )
    }
}
