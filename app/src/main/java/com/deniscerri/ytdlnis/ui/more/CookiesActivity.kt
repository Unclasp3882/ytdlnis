package com.deniscerri.ytdlnis.ui.more

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deniscerri.ytdlnis.R
import com.deniscerri.ytdlnis.adapter.CookieAdapter
import com.deniscerri.ytdlnis.database.models.CookieItem
import com.deniscerri.ytdlnis.database.viewmodel.CookieViewModel
import com.deniscerri.ytdlnis.util.FileUtil
import com.deniscerri.ytdlnis.util.UiUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.net.CookieManager


class CookiesActivity : AppCompatActivity(), CookieAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var listAdapter: CookieAdapter
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var cookiesViewModel: CookieViewModel
    private lateinit var uiUtil: UiUtil
    var context: Context? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cookies)
        context = baseContext

        topAppBar = findViewById(R.id.logs_toolbar)
        topAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        listAdapter =
            CookieAdapter(
                this,
                this@CookiesActivity
            )
        recyclerView = findViewById(R.id.template_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter

        uiUtil = UiUtil(FileUtil())

        cookiesViewModel = ViewModelProvider(this)[CookieViewModel::class.java]
        cookiesViewModel.items.observe(this) {
            listAdapter.submitList(it)
        }
        initMenu()
        initChips()
    }

    private fun initMenu() {
        topAppBar.setOnMenuItemClickListener { m: MenuItem ->
            when (m.itemId) {
                R.id.delete_cookies -> {
                    val deleteDialog = MaterialAlertDialogBuilder(this)
                    deleteDialog.setTitle(getString(R.string.confirm_delete_history))
                    deleteDialog.setMessage(getString(R.string.confirm_delete_cookies_desc))
                    deleteDialog.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
                    deleteDialog.setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                        android.webkit.CookieManager.getInstance().removeAllCookies(null)
                        kotlin.runCatching {
                            File(cacheDir, "cookies.txt").apply { writeText("") }
                        }
                    }
                    deleteDialog.show()
                }
                R.id.export_clipboard -> {
                    cookiesViewModel.exportToClipboard()
                }
            }
            true
        }
    }

    private fun initChips() {
        val new = findViewById<Chip>(R.id.newCookie)
        new.setOnClickListener {
            showDialog("")
        }
    }

    private fun showDialog(url: String){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.cookies))
        val inputLayout = layoutInflater.inflate(R.layout.textinput, null)
        val editText = inputLayout.findViewById<EditText>(R.id.url_edittext)
        editText.setText(url)
        builder.setView(inputLayout)
        builder.setPositiveButton(
            getString(R.string.get_cookies)
        ) { dialog: DialogInterface?, which: Int ->
            val myIntent = Intent(this, WebViewActivity::class.java)
            myIntent.putExtra("url", editText.text.toString())
            startActivity(myIntent)
        }

        // handle the negative button of the alert dialog
        builder.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog: DialogInterface?, which: Int -> }
        val dialog = builder.create()
        editText.setOnKeyListener { view, i, keyEvent ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editText.text.isNotEmpty()
            false
        }
        dialog.show()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        editText!!.postDelayed({
            editText.requestFocus()
            imm.showSoftInput(editText, 0)
        }, 300)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editText.text.isNotEmpty()
    }

    companion object {
        private const val TAG = "CookiesActivity"
    }

    override fun onItemClick(cookie: CookieItem) {
        showDialog(cookie.url)
    }

    override fun onSelected(cookie: CookieItem) {
        TODO("Not yet implemented")
    }

    override fun onDelete(cookie: CookieItem) {
        val deleteDialog = MaterialAlertDialogBuilder(this)
        deleteDialog.setTitle(getString(R.string.you_are_going_to_delete) + " \"" + cookie.url + "\"!")
        deleteDialog.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        deleteDialog.setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
            cookiesViewModel.delete(cookie)
        }
        deleteDialog.show()
    }
}