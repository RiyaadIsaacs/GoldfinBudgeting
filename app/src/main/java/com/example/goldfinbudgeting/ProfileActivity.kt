package com.example.goldfinbudgeting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.io.File

class ProfileActivity : AppCompatActivity() {

    //open the device photo picker. no storage permission needed
    private val pickProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            saveProfilePicture(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_profile)

        //keep content from hiding behind status bar like the other screens
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        //find drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find hamburger menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home tab button
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find expenses tab button
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find envelopes in the drawer
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //profile form fields
        val displayNameHeading = findViewById<TextView>(R.id.displayNameHeading)
        val displayNameEditText = findViewById<EditText>(R.id.displayNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val mobileEditText = findViewById<EditText>(R.id.mobileEditText)

        //load any saved profile values
        val profilePrefs = getSharedPreferences("goldfin_profile", MODE_PRIVATE)

        displayNameEditText.setText(profilePrefs.getString("display_name", ""))
        emailEditText.setText(AccountStore.email(this))
        mobileEditText.setText(profilePrefs.getString("mobile", ""))

        val savedName = profilePrefs.getString("display_name", "")
        if (!savedName.isNullOrBlank()) {
            displayNameHeading.text = savedName
        }

        val profileImage = findViewById<ImageView>(R.id.profileImage)

        profileImage.clipToOutline = true
        loadProfilePicture(profileImage)

        //tap the avatar to pick a photo from the device
        profileImage.setOnClickListener {
            pickProfileImage.launch("image/*")
        }

        //open side menu
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close side menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //go back to log in screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //close profile screen and go back to home
        homeTab.setOnClickListener {
            finish()
        }

        //open expenses screen
        expensesTab.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)

            startActivity(intent)
        }

        //open envelopes screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }

        //save display name and update the heading
        findViewById<TextView>(R.id.saveDisplayNameButton).setOnClickListener {
            val name = displayNameEditText.text.toString().trim()

            profilePrefs.edit().putString("display_name", name).apply()

            displayNameHeading.text = if (name.isEmpty()) "MyName" else name

            Toast.makeText(this, "Display name saved", Toast.LENGTH_SHORT).show()
        }

        //save email address and keep login in sync
        findViewById<TextView>(R.id.saveEmailButton).setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            } else {
                AccountStore.setEmail(this, email)

                Toast.makeText(this, "Email saved", Toast.LENGTH_SHORT).show()
            }
        }

        // Change Password
        // opens the first popup asking for the current password
        findViewById<TextView>(R.id.changePasswordButton).setOnClickListener {
            showCurrentPasswordPopup()
        }

        //save mobile number
        findViewById<TextView>(R.id.saveMobileButton).setOnClickListener {
            profilePrefs.edit().putString("mobile", mobileEditText.text.toString().trim()).apply()

            Toast.makeText(this, "Mobile number saved", Toast.LENGTH_SHORT).show()
        }

        //security policy placeholder until that page is built
        findViewById<LinearLayout>(R.id.securityPolicyButton).setOnClickListener {
            Toast.makeText(this, "Security Policy coming soon", Toast.LENGTH_SHORT).show()
        }

        //account settings placeholder until that page is built
        findViewById<LinearLayout>(R.id.accountSettingsButton).setOnClickListener {
            Toast.makeText(this, "Account Settings coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // Step 1
    // ask for the current password. Confirm continues. Cancel aborts
    private fun showCurrentPasswordPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_current_password, null)
        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPasswordEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel") { popup, _ ->
                // Cancel closes the popup and stops the password change
                popup.dismiss()
            }
            .create()

        // set Confirm here so a wrong password does not auto close the dialog
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val typedPassword = currentPasswordEditText.text.toString()

                if (typedPassword == AccountStore.password(this)) {
                    // correct so close this popup and open the new password popup
                    dialog.dismiss()

                    showNewPasswordPopup()
                } else {
                    Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    // Step 2
    // ask for new password and confirm password. save only if they match
    private fun showNewPasswordPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_password, null)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel") { popup, _ ->
                // Cancel aborts the whole password change
                popup.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    // both match so update the password stored in Room
                    AccountStore.setPassword(this, newPassword)

                    dialog.dismiss()

                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun profilePictureFile(): File {
        return File(filesDir, "profile_picture.jpg")
    }

    //copy the chosen photo into app storage so it stays after closing the app
    private fun saveProfilePicture(uri: Uri) {
        try {
            val pictureFile = profilePictureFile()

            contentResolver.openInputStream(uri)?.use { input ->
                pictureFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            getSharedPreferences("goldfin_profile", MODE_PRIVATE)
                .edit()
                .putString("profile_picture", pictureFile.absolutePath)
                .apply()

            loadProfilePicture(findViewById(R.id.profileImage))

            Toast.makeText(this, "Profile picture saved", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not save that photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePicture(profileImage: ImageView) {
        val pictureFile = profilePictureFile()

        if (pictureFile.exists()) {
            profileImage.setPadding(0, 0, 0, 0)
            profileImage.setImageURI(null)
            profileImage.setImageURI(Uri.fromFile(pictureFile))
        } else {
            profileImage.setPadding(28, 28, 28, 28)
            profileImage.setImageResource(R.drawable.ic_profile_person)
        }
    }
}
