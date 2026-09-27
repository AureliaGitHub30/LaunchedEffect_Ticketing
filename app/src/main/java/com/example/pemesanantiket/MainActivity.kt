package com.example.pemesanantiket

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val colorBgStart = Color.parseColor("#E9F3D6")
    private val colorBgEnd = Color.parseColor("#D6D4F2")
    private val colorCardWhite = Color.parseColor("#FFFFFF")
    private val colorTextDark = Color.parseColor("#1E2749")
    private val colorTextGray = Color.parseColor("#8B90A8")
    private val colorInputBg = Color.parseColor("#F4F5FA")
    private val colorInputBorder = Color.parseColor("#E3E5F1")
    private val colorAccent = Color.parseColor("#6C7FE0")
    private val colorAccentDark = Color.parseColor("#4F5FC7")
    private val colorBtnDisabled = Color.parseColor("#C7CCE8")
    private val colorSuccessBg = Color.parseColor("#DFF5E3")
    private val colorSuccessText = Color.parseColor("#2E9E52")
    private val colorErrorBg = Color.parseColor("#FCE4E4")
    private val colorErrorText = Color.parseColor("#D8453C")
    private val colorProcessingBg = Color.parseColor("#E3E8FB")
    private val colorProcessingText = Color.parseColor("#4F5FC7")
    private val colorIdleBg = Color.parseColor("#F1F2F8")
    private val colorIdleText = Color.parseColor("#6A6F87")


    private data class TanggalItem(val tanggal: String, val hari: String)

    private val daftarTanggal = listOf(
        TanggalItem("15", "SAB"),
        TanggalItem("16", "MIN"),
        TanggalItem("17", "SEN"),
        TanggalItem("18", "SEL"),
        TanggalItem("19", "RAB")
    )

    private var selectedDateIndex = 2
    private var jumlahTiket = 1
    private val hargaPerTiket = 100_000
    private var isProcessing = false

    // ---------- View refs ----------
    private lateinit var tvJumlah: TextView
    private lateinit var tvHargaSatuan: TextView
    private lateinit var tvTotal: TextView
    private lateinit var etNama: EditText
    private lateinit var btnPesan: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressStatus: ProgressBar
    private lateinit var statusContainer: LinearLayout
    private lateinit var dateContainer: LinearLayout
    private val dateChipViews = mutableListOf<TextView>()


    private fun rounded(radiusDp: Float, fillColor: Int, strokeColor: Int? = null, strokeWidthDp: Float = 0f): GradientDrawable {
        val d = GradientDrawable()
        d.cornerRadius = dp(radiusDp)
        d.setColor(fillColor)
        if (strokeColor != null) d.setStroke(dp(strokeWidthDp).toInt(), strokeColor)
        return d
    }

    private fun oval(fillColor: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(fillColor)
        return d
    }

    private fun gradientBg(): GradientDrawable {
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(colorBgStart, colorBgEnd))
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun dpInt(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        buatDateChips()
        updateHargaDanTotal()
    }

    // ---------- Bangun seluruh UI di sini ----------
    private fun buildUi(): View {
        val root = FrameLayout(this).apply {
            background = gradientBg()
        }

        val scroll = NestedScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dpInt(20), dpInt(20), dpInt(20), dpInt(20))
        }

        // ---- Header ----
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpInt(12), 0, dpInt(20))
        }

        val title = TextView(this).apply {
            text = "Pemesanan Tiket"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colorTextDark)
        }

        header.addView(title)
        content.addView(header)

        val dateScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpInt(20) }
        }
        dateContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        dateScroll.addView(dateContainer)
        content.addView(dateScroll)

        // ---- Card ----
        val card = CardView(this).apply {
            radius = dp(20f)
            cardElevation = dp(6f)
            setCardBackgroundColor(colorCardWhite)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val cardInner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val imgFoto = ImageView(this).apply {
            setImageResource(R.drawable.jatim_park)
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpInt(160)
            )
        }
        cardInner.addView(imgFoto)

        val cardBody = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpInt(20), dpInt(16), dpInt(20), dpInt(20))
        }

        // Nama taman (opsional, tetap dari contoh)
        cardBody.addView(TextView(this).apply {
            text = "Jatim Park 1"
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colorAccentDark)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpInt(8) }
        })

        cardBody.addView(labelText("Nama"))

        etNama = EditText(this).apply {
            hint = "Masukkan nama Anda"
            setHintTextColor(colorTextGray)
            setTextColor(colorTextDark)
            textSize = 12f
            setSingleLine(true)
            background = rounded(10f, colorInputBg, colorInputBorder, 1f)
            setPadding(dpInt(10), 0, dpInt(10), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpInt(48)
            ).apply { bottomMargin = dpInt(16) }
        }
        cardBody.addView(etNama)

        cardBody.addView(labelText("Jumlah Tiket", bottomMarginDp = 10))

        val counterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpInt(20) }
        }
        val btnMinus = counterButton("−") { ubahJumlah(-1) }
        tvJumlah = TextView(this).apply {
            text = "1"
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colorTextDark)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btnPlus = counterButton("+") { ubahJumlah(1) }
        counterRow.addView(btnMinus)
        counterRow.addView(tvJumlah)
        counterRow.addView(btnPlus)
        cardBody.addView(counterRow)
        cardBody.addView(priceRow("Harga Tiket", colorTextGray, 14f, false).also {
            tvHargaSatuan = it.getChildAt(1) as TextView
        })
        cardBody.addView(priceRow("Total", colorTextDark, 15f, true, bottomMarginDp = 20).also {
            tvTotal = it.getChildAt(1) as TextView
            tvTotal.setTextColor(colorAccentDark)
        })

        btnPesan = Button(this).apply {
            text = "Pesan Tiket"
            isAllCaps = false
            setTextColor(colorCardWhite)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            background = rounded(16f, colorAccent)
            elevation = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpInt(52)
            ).apply { bottomMargin = dpInt(16) }
            setOnClickListener { prosesPesanTiket() }
        }
        cardBody.addView(btnPesan)

        // Status box
        statusContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(12f, colorIdleBg)
            setPadding(dpInt(12), dpInt(12), dpInt(12), dpInt(12))
        }
        progressStatus = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(dpInt(18), dpInt(18)).apply { marginEnd = dpInt(8) }
        }
        tvStatus = TextView(this).apply {
            text = "Status: Silakan pesan tiket"
            textSize = 13f
            setTextColor(colorIdleText)
        }
        statusContainer.addView(progressStatus)
        statusContainer.addView(tvStatus)
        cardBody.addView(statusContainer)

        cardInner.addView(cardBody)
        card.addView(cardInner)
        content.addView(card)

        scroll.addView(content)
        root.addView(scroll)
        return root
    }

    private fun labelText(text: String, bottomMarginDp: Int = 8): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(colorTextDark)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpInt(bottomMarginDp) }
        }
    }

    private fun counterButton(symbol: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colorTextDark)
            gravity = Gravity.CENTER
            background = oval(colorInputBg)
            layoutParams = LinearLayout.LayoutParams(dpInt(40), dpInt(40))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun priceRow(label: String, labelColor: Int, labelSize: Float, bold: Boolean, bottomMarginDp: Int = 6): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpInt(bottomMarginDp) }
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(labelColor)
            textSize = labelSize
            if (bold) setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvValue = TextView(this).apply {
            text = "Rp 0"
            setTextColor(colorTextDark)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
        }
        row.addView(tvLabel)
        row.addView(tvValue)
        return row
    }

    // ---------- Date chips ----------
    private fun buatDateChips() {
        dateContainer.removeAllViews()
        dateChipViews.clear()

        daftarTanggal.forEachIndexed { index, item ->
            val chip = TextView(this).apply {
                text = "${item.tanggal}\n${item.hari}"
                gravity = Gravity.CENTER
                textSize = 13f
                setPadding(dpInt(18), dpInt(14), dpInt(18), dpInt(14))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dpInt(12) }
                setOnClickListener {
                    selectedDateIndex = index
                    updateDateChipStyles()
                }
            }
            dateChipViews.add(chip)
            dateContainer.addView(chip)
        }
        updateDateChipStyles()
    }

    private fun updateDateChipStyles() {
        dateChipViews.forEachIndexed { index, chip ->
            if (index == selectedDateIndex) {
                chip.background = rounded(14f, colorAccent)
                chip.setTextColor(colorCardWhite)
            } else {
                chip.background = rounded(14f, colorCardWhite, colorInputBorder, 1f)
                chip.setTextColor(colorTextDark)
            }
        }
    }

    // ---------- Logika jumlah tiket & harga ----------
    private fun ubahJumlah(delta: Int) {
        if (isProcessing) return
        val baru = jumlahTiket + delta
        if (baru < 1) return
        jumlahTiket = baru
        tvJumlah.text = jumlahTiket.toString()
        updateHargaDanTotal()
    }

    private fun updateHargaDanTotal() {
        tvHargaSatuan.text = formatRupiah(hargaPerTiket)
        tvTotal.text = formatRupiah(hargaPerTiket * jumlahTiket)
    }

    private fun formatRupiah(nominal: Int): String {
        val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
        return "Rp ${format.format(nominal)}"
    }

    // ---------- Proses pesan tiket ----------
    private fun prosesPesanTiket() {
        if (isProcessing) return

        val nama = etNama.text.toString().trim()
        if (nama.isEmpty()) {
            etNama.background = rounded(12f, colorInputBg, colorErrorText, 1.5f)
            setStatus(
                text = "Status: Nama harus diisi",
                bgColor = colorErrorBg,
                textColor = colorErrorText,
                showProgress = false
            )
            return
        }

        etNama.background = rounded(12f, colorInputBg, colorInputBorder, 1f)
        isProcessing = true
        btnPesan.isEnabled = false
        btnPesan.background = rounded(16f, colorBtnDisabled)

        setStatus(
            text = "Status: Memproses pesanan...",
            bgColor = colorProcessingBg,
            textColor = colorProcessingText,
            showProgress = true
        )

        // Simulasi proses selama 2 detik
        Handler(Looper.getMainLooper()).postDelayed({
            isProcessing = false
            btnPesan.isEnabled = true
            btnPesan.background = rounded(16f, colorAccent)

            setStatus(
                text = "Status: Tiket berhasil dipesan!",
                bgColor = colorSuccessBg,
                textColor = colorSuccessText,
                showProgress = false
            )
        }, 2000)
    }

    private fun setStatus(text: String, bgColor: Int, textColor: Int, showProgress: Boolean) {
        tvStatus.text = text
        tvStatus.setTextColor(textColor)
        statusContainer.background = rounded(12f, bgColor)
        progressStatus.visibility = if (showProgress) View.VISIBLE else View.GONE
    }
}