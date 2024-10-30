package com.example.currencyconverter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.currencyconverter.databinding.ActivityMainBinding
import com.example.currencyconverter.data.ExchangeRates


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isSourceInput = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val currencies = listOf("USD", "VND", "EUR", "JPY", "GBP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrom.adapter = adapter
        binding.spinnerTo.adapter = adapter


        binding.amountInput.setOnClickListener {
            isSourceInput = true // Đặt ô này là nguồn
        }

        binding.resultText.setOnClickListener {
            isSourceInput = false // Đặt ô này là đích
        }

        // Lắng nghe sự kiện thay đổi trong EditText
        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isSourceInput) {
                    convertCurrencyFromInput()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Lắng nghe sự kiện thay đổi trong spinnerFrom
        binding.spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (isSourceInput) {
                    convertCurrencyFromInput()
                    updateSymbol(binding.spinnerFrom.selectedItem.toString(), binding.symbolFrom)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        binding.spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (!isSourceInput) {
                    convertCurrencyFromResult()
                    updateSymbol(binding.spinnerTo.selectedItem.toString(), binding.symbolTo)
                }
                updateSymbol(binding.spinnerTo.selectedItem.toString(), binding.symbolTo)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        updateSymbol(binding.spinnerFrom.selectedItem.toString(), binding.symbolFrom)

    }

    private fun convertCurrencyFromInput() {
        val from = binding.spinnerFrom.selectedItem.toString()
        val to = binding.spinnerTo.selectedItem.toString()
        val amountText = binding.amountInput.text.toString()


        if (amountText.isEmpty()) {
            binding.resultText.text = "0.00"
            binding.exchangeRateText.text = ""
            return
        }

        val amount = amountText.toDoubleOrNull() ?: return
        if (amount > 0) {
            convertCurrency(from, to, amount)
        } else {
            binding.resultText.text = "0.00" // Nếu amount <= 0, hiển thị 0.00
            binding.exchangeRateText.text = "" // Xóa tỷ giá
        }
    }

    private fun convertCurrencyFromResult() {
        val from = binding.spinnerTo.selectedItem.toString()
        val to = binding.spinnerFrom.selectedItem.toString()
        val amountText = binding.resultText.text.toString()

        // Kiểm tra nếu không có giá trị trong resultText
        if (amountText.isEmpty() || amountText == "0.00") {
            binding.amountInput.text.clear() // Xóa ô nhập
            binding.amountInput.hint = "0" // Hiển thị 0
            binding.exchangeRateText.text = "" // Xóa tỷ giá
            return // Trở về để không thực hiện chuyển đổi
        }

        val amount = amountText.toDoubleOrNull() ?: return // Kiểm tra giá trị null
        if (amount > 0) { // Chỉ thực hiện chuyển đổi nếu amount > 0
            convertCurrency(from, to, amount, reverse = true)
        }
    }

    private fun convertCurrency(from: String, to: String, amount: Double, reverse: Boolean = false) {
        val key = if (reverse) "${to}_$from" else "${from}_$to"
        val rate = ExchangeRates.rates[key] ?: 1.00 // Nếu không tìm thấy tỷ giá, sử dụng 1.00

        // Tính toán số tiền đã chuyển đổi
        val convertedAmount = amount * rate

        // Cập nhật giao diện người dùng
        if (reverse) {
            binding.amountInput.text = Editable.Factory.getInstance().newEditable(String.format("%.2f", convertedAmount))
        } else {
            binding.resultText.text = String.format("%.2f", convertedAmount)
            binding.exchangeRateText.text = "1 $from = ${String.format("%.6f", rate)} $to" // Cập nhật tỷ giá tương ứng
        }
    }

    private fun updateSymbol(currency: String, textView: TextView) {
        val symbol = when (currency) {
            "USD" -> "$"
            "VND" -> "₫"
            "EUR" -> "€"
            "JPY" -> "¥"
            "GBP" -> "£"
            else -> ""
        }
        textView.text = symbol
    }

}
