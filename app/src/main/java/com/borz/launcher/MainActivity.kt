package com.borz.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

data class AppItem(val label: String, val pkg: String, val icon: Drawable)

class MainActivity : Activity() {
    private var all = listOf<AppItem>()
    private var shown = listOf<AppItem>()
    private lateinit var adapter: AppAdapter
    private lateinit var search: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val d = resources.displayMetrics.density
        fun px(v: Int) = (v * d).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(16), px(48), px(16), px(16))
            setBackgroundColor(0x88000000.toInt())
        }
        val clock = TextClock(this).apply {
            format12Hour = "HH:mm"; format24Hour = "HH:mm"
            textSize = 56f; setTextColor(Color.WHITE)
        }
        val date = TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"; format24Hour = "EEEE, d MMMM"
            textSize = 16f; setTextColor(Color.LTGRAY)
        }
        search = EditText(this).apply {
            hint = "Qidirish..."
            setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE)
            isSingleLine = true
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { filter(s.toString()) }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        adapter = AppAdapter()
        val grid = GridView(this).apply {
            numColumns = 5
            verticalSpacing = px(12)
            this.adapter = this@MainActivity.adapter
            setOnItemClickListener { _, _, i, _ ->
                packageManager.getLaunchIntentForPackage(shown[i].pkg)?.let { startActivity(it) }
            }
        }
        root.addView(clock); root.addView(date); root.addView(search)
        root.addView(grid, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        all = packageManager.queryIntentActivities(i, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { AppItem(it.loadLabel(packageManager).toString(), it.activityInfo.packageName, it.loadIcon(packageManager)) }
            .sortedBy { it.label.lowercase() }
        search.setText("")
        filter("")
    }

    private fun filter(q: String) {
        shown = all.filter { it.label.contains(q, ignoreCase = true) }
        adapter.notifyDataSetChanged()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { search.setText("") }

    inner class AppAdapter : BaseAdapter() {
        override fun getCount() = shown.size
        override fun getItem(p: Int) = shown[p]
        override fun getItemId(p: Int) = p.toLong()
        override fun getView(p: Int, v: View?, parent: ViewGroup?): View {
            val d = resources.displayMetrics.density
            val item = shown[p]
            val col = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
            col.addView(ImageView(this@MainActivity).apply { setImageDrawable(item.icon) },
                LinearLayout.LayoutParams((48 * d).toInt(), (48 * d).toInt()))
            col.addView(TextView(this@MainActivity).apply {
                text = item.label; textSize = 11f; setTextColor(Color.WHITE)
                maxLines = 1; gravity = Gravity.CENTER
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            return col
        }
    }
}
