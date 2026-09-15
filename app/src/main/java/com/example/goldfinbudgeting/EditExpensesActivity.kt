package com.example.goldfinbudgeting

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.io.File
import java.util.Calendar

class EditExpensesActivity : AppCompatActivity() {

    private var selectedCategory = "General"
    private var receiptPath: String? = null

    //null means the form is creating a new expense
    private var editingExpenseId: Long? = null

    //keep edit list on the same month as the main expenses screen
    private var selectedYear = 2026
    private var selectedMonth = Calendar.AUGUST

    //open the device photo picker. no storage permission needed
    private val pickReceiptImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            saveReceiptPhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_edit_expenses)

        //prevent content from hiding behind status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        //find drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find burger menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close drawer button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home button
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find expenses button
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find profile button
        val profileTab = findViewById<TextView>(R.id.profileTab)

        //find envelopes drawer button
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //find cancel button
        val cancelButton = findViewById<TextView>(R.id.cancelButton)

        //find add/save button
        val addButton = findViewById<TextView>(R.id.addButton)

        //form fields
        val entryNameEditText = findViewById<EditText>(R.id.entryNameEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val datePickerRow = findViewById<LinearLayout>(R.id.datePickerRow)
        val calendarButton = findViewById<TextView>(R.id.calendarButton)
        val categoryPickerRow = findViewById<LinearLayout>(R.id.categoryPickerRow)
        val categoryText = findViewById<TextView>(R.id.categoryText)
        val receiptPhotoButton = findViewById<FrameLayout>(R.id.receiptPhotoButton)

        applyMonthFromIntent(intent)

        //default date matches the month being edited
        dateEditText.setText(ExpenseTempMemory.formatDate(ExpenseTempMemory.dateOn(selectedYear, selectedMonth, 14)))
        categoryText.text = selectedCategory

        //open burger menu
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close burger menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //go to login screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //close current active screen and go home
        homeTab.setOnClickListener {
            finish()
        }

        //open expenses screen
        expensesTab.setOnClickListener {
            finish()
        }

        //open profile screen
        profileTab.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

        //open envelopes screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }

        //cancel discards unsaved form changes and goes back. expense data in memory stays intact
        cancelButton.setOnClickListener {
            finish()
        }

        //open a calendar when they tap the date field or calendar icon
        val openDatePicker = View.OnClickListener {
            showDatePicker(dateEditText)
        }

        datePickerRow.setOnClickListener(openDatePicker)
        dateEditText.setOnClickListener(openDatePicker)
        calendarButton.setOnClickListener(openDatePicker)

        //pick a category from the list used on the expenses chips
        categoryPickerRow.setOnClickListener {
            showCategoryPicker(categoryText)
        }

        //tap the dashed box to attach or view a receipt photo
        receiptPhotoButton.setOnClickListener {
            if (ReceiptViewer.hasReceipt(receiptPath)) {
                AlertDialog.Builder(this)
                    .setTitle("Receipt photo")
                    .setItems(arrayOf("View photo", "Replace photo")) { _, which ->
                        if (which == 0) {
                            ReceiptViewer.show(this, receiptPath)
                        } else {
                            pickReceiptImage.launch("image/*")
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                pickReceiptImage.launch("image/*")
            }
        }

        //save a new expense or update the one being edited
        addButton.setOnClickListener {
            saveExpenseFromForm()
        }

        //if the main screen asked to edit a specific expense, load it now
        val expenseId = intent.getLongExtra("expense_id", -1L)

        if (expenseId > 0L) {
            loadExpenseIntoForm(expenseId)
        } else {
            //older callers may still send a list index
            val expenseIndex = intent.getIntExtra("expense_index", -1)

            if (expenseIndex >= 0 && expenseIndex < ExpenseTempMemory.expenses.size) {
                loadExpenseIntoForm(ExpenseTempMemory.expenses[expenseIndex].id)
            }
        }

        refreshEditableExpenseList()
    }

    override fun onResume() {
        super.onResume()

        refreshEditableExpenseList()
    }

    private fun saveExpenseFromForm() {
        val entryNameEditText = findViewById<EditText>(R.id.entryNameEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val startTimeEditText = findViewById<EditText>(R.id.startTimeEditText)
        val endTimeEditText = findViewById<EditText>(R.id.endTimeEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)

        val name = entryNameEditText.text.toString().trim()
        val amount = amountEditText.text.toString().toDoubleOrNull()
        val dateCreated = ExpenseTempMemory.parseDate(dateEditText.text.toString())
        val description = descriptionEditText.text.toString().trim()
        val startTime = startTimeEditText.text.toString().trim()
        val endTime = endTimeEditText.text.toString().trim()

        if (name.isBlank()) {
            Toast.makeText(this, "Please enter an entry name", Toast.LENGTH_SHORT).show()

            return
        }

        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()

            return
        }

        val expense = Expense(
            name = name,
            amount = amount,
            category = selectedCategory,
            dateCreated = dateCreated,
            description = description,
            startTime = startTime,
            endTime = endTime,
            receiptPath = receiptPath
        )

        val currentEditId = editingExpenseId

        if (currentEditId != null) {
            if (!ExpenseTempMemory.updateExpenseById(currentEditId, expense)) {
                Toast.makeText(this, "Could not update that expense", Toast.LENGTH_SHORT).show()

                return
            }

            Toast.makeText(this, "Updated $name", Toast.LENGTH_SHORT).show()
        } else {
            ExpenseTempMemory.addExpense(expense)

            Toast.makeText(this, "Added $name", Toast.LENGTH_SHORT).show()
        }

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = dateCreated

        val intent = Intent(this, ExpensesActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("filter_year", calendar.get(Calendar.YEAR))
        intent.putExtra("filter_month", calendar.get(Calendar.MONTH))

        startActivity(intent)

        finish()
    }

    private fun loadExpenseIntoForm(expenseId: Long) {
        val expense = ExpenseTempMemory.expenseById(expenseId) ?: return
        val entryNameEditText = findViewById<EditText>(R.id.entryNameEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val startTimeEditText = findViewById<EditText>(R.id.startTimeEditText)
        val endTimeEditText = findViewById<EditText>(R.id.endTimeEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val categoryText = findViewById<TextView>(R.id.categoryText)
        val addButton = findViewById<TextView>(R.id.addButton)

        editingExpenseId = expense.id
        selectedCategory = expense.category
        receiptPath = expense.receiptPath

        entryNameEditText.setText(expense.name)
        amountEditText.setText(String.format(java.util.Locale.US, "%.2f", expense.amount))
        dateEditText.setText(ExpenseTempMemory.formatDate(expense.dateCreated))
        startTimeEditText.setText(if (expense.startTime.isBlank()) "09:00" else expense.startTime)
        endTimeEditText.setText(if (expense.endTime.isBlank()) "17:00" else expense.endTime)
        descriptionEditText.setText(expense.description)
        categoryText.text = selectedCategory
        addButton.text = "Save"

        showReceiptPreview(expense.receiptPath)

        //keep the form in view while editing
        findViewById<View>(R.id.entryNameEditText).requestFocus()
    }

    private fun clearFormForNewExpense() {
        val entryNameEditText = findViewById<EditText>(R.id.entryNameEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val startTimeEditText = findViewById<EditText>(R.id.startTimeEditText)
        val endTimeEditText = findViewById<EditText>(R.id.endTimeEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val categoryText = findViewById<TextView>(R.id.categoryText)
        val addButton = findViewById<TextView>(R.id.addButton)

        editingExpenseId = null
        selectedCategory = "General"
        receiptPath = null

        entryNameEditText.setText("")
        amountEditText.setText("")
        dateEditText.setText(ExpenseTempMemory.formatDate(ExpenseTempMemory.dateOn(selectedYear, selectedMonth, 14)))
        startTimeEditText.setText("09:00")
        endTimeEditText.setText("17:00")
        descriptionEditText.setText("")
        categoryText.text = selectedCategory
        addButton.text = "Add"

        showReceiptPreview(null)
    }

    private fun applyMonthFromIntent(intent: Intent) {
        if (intent.hasExtra("filter_year") && intent.hasExtra("filter_month")) {
            selectedYear = intent.getIntExtra("filter_year", selectedYear)
            selectedMonth = intent.getIntExtra("filter_month", selectedMonth)
        }
    }

    private fun expenseIsInSelectedMonth(expense: Expense): Boolean {
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = expense.dateCreated

        return calendar.get(Calendar.YEAR) == selectedYear &&
            calendar.get(Calendar.MONTH) == selectedMonth
    }

    private fun refreshEditableExpenseList() {
        val container = findViewById<LinearLayout>(R.id.editExpenseListContainer)

        container.removeAllViews()

        //only show expenses for the month opened from the main screen
        val monthExpenses = ExpenseTempMemory.expenses.filter { expense ->
            expenseIsInSelectedMonth(expense)
        }

        monthExpenses.forEachIndexed { rowIndex, expense ->
            val row = layoutInflater.inflate(R.layout.item_edit_expense_row, container, false)

            val nameText = row.findViewById<TextView>(R.id.editExpenseNameText)
            val metaText = row.findViewById<TextView>(R.id.editExpenseMetaText)
            val receiptButton = row.findViewById<TextView>(R.id.editExpenseReceiptButton)
            val editButton = row.findViewById<TextView>(R.id.editExpenseEditButton)
            val deleteButton = row.findViewById<TextView>(R.id.editExpenseDeleteButton)
            val divider = row.findViewById<View>(R.id.editExpenseRowDivider)

            nameText.text = expense.name
            metaText.text = ExpenseTempMemory.expenseSubtitle(expense)

            if (rowIndex == monthExpenses.lastIndex) {
                divider.visibility = View.GONE
            }

            if (ReceiptViewer.hasReceipt(expense.receiptPath)) {
                receiptButton.visibility = View.VISIBLE
                receiptButton.setOnClickListener {
                    ReceiptViewer.show(this, expense.receiptPath)
                }
            } else {
                receiptButton.visibility = View.GONE
                receiptButton.setOnClickListener(null)
            }

            val startEdit = View.OnClickListener {
                loadExpenseIntoForm(expense.id)
            }

            row.setOnClickListener(startEdit)
            editButton.setOnClickListener(startEdit)

            deleteButton.setOnClickListener {
                confirmDeleteExpense(expense.id, expense.name)
            }

            container.addView(row)
        }
    }

    private fun confirmDeleteExpense(expenseId: Long, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete expense")
            .setMessage("Are you sure you wish to delete \"$name\"?")
            .setPositiveButton("Delete") { _, _ ->
                val wasEditingThis = editingExpenseId == expenseId

                if (ExpenseTempMemory.deleteExpenseById(expenseId)) {
                    Toast.makeText(this, "Deleted $name", Toast.LENGTH_SHORT).show()

                    if (wasEditingThis) {
                        clearFormForNewExpense()
                    }

                    refreshEditableExpenseList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(dateEditText: EditText) {
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = ExpenseTempMemory.parseDate(dateEditText.text.toString())

        DatePickerDialog(
            this,
            { _, year, month, day ->
                dateEditText.setText(
                    ExpenseTempMemory.formatDate(ExpenseTempMemory.dateOn(year, month, day))
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showCategoryPicker(categoryText: TextView) {
        val options = ExpenseTempMemory.categories.toTypedArray()
        val checkedItem = options.indexOf(selectedCategory).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Choose category")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                selectedCategory = options[which]
                categoryText.text = selectedCategory
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveReceiptPhoto(uri: Uri) {
        try {
            val pictureFile = File(filesDir, "receipt_${System.currentTimeMillis()}.jpg")

            contentResolver.openInputStream(uri)?.use { input ->
                pictureFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            receiptPath = pictureFile.absolutePath
            showReceiptPreview(receiptPath)

            Toast.makeText(this, "Receipt photo attached", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not attach that photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReceiptPreview(path: String?) {
        val receiptImage = findViewById<ImageView>(R.id.receiptImage)
        val receiptPlaceholder = findViewById<LinearLayout>(R.id.receiptPlaceholder)

        if (path.isNullOrBlank()) {
            receiptImage.setImageURI(null)
            receiptImage.visibility = View.GONE
            receiptPlaceholder.visibility = View.VISIBLE

            return
        }

        val pictureFile = File(path)

        if (!pictureFile.exists()) {
            receiptImage.setImageURI(null)
            receiptImage.visibility = View.GONE
            receiptPlaceholder.visibility = View.VISIBLE

            return
        }

        receiptPlaceholder.visibility = View.GONE
        receiptImage.visibility = View.VISIBLE
        receiptImage.setImageURI(null)
        receiptImage.setImageURI(Uri.fromFile(pictureFile))
    }
}
